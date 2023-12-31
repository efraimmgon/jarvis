(ns jarvis.utils.fsdb
  (:require
    clojure.edn
    clojure.pprint
    [clojure.java.io :as io]
    [me.raynes.fs :as fs])
  (:import
    java.time.Instant))

;;; ----------------------------------------------------------------------------
;;; Filesystem-based Database
;;; ----------------------------------------------------------------------------

;;; ----------------------------------------------------------------------------
;;; Utils
;;; ----------------------------------------------------------------------------


;; So that java.time.Instant prints as a inst literal
(defmethod print-method Instant [obj ^java.io.Writer w]
  (.write w (str "#inst \"" (.toString obj) "\"")))


;;; All the data is placed inside the resources/db folder, by default.
(def db-dir (io/file fs/*cwd* "resources" "db"))
(def settings-path (io/file db-dir "settings.edn"))


(defn load-edn
  "Load edn from an io/reader source (filename or io/resource).
   `source` is a java.io.File."
  [source]
  (try
    (with-open [r (io/reader source)]
      (clojure.edn/read
        {:readers {'inst #(Instant/parse %)}}
        (java.io.PushbackReader.
          r)))

    (catch java.io.IOException e
      (printf "Couldn't open '%s': %s\n" source (.getMessage e)))
    (catch RuntimeException e
      (printf "Error parsing edn file '%s': %s\n" source (.getMessage e)))))


(defn save-edn!
  "Save edn data to file.
   `file` is a java.io.File.
   `data` is a map.
   `opts` is a map of #{:pretty-print}."
  ([file data] (save-edn! file data nil))
  ([file data opts]
   (with-open [wrt (io/writer file)]
     (binding [*out* wrt]
       (if (:pretty-print? opts)
         (clojure.pprint/pprint data)
         (prn data))))
   data))


(declare settings)


(defn parse-coll
  [x]
  (condp #(%1 %2) x
    string? x
    keyword? (name x)
    symbol? (name x)
    (str x)))

(defn resolve-path
  "Returns a vector with the path to the collection's dir.
   `path` is a scalar or a vector of them."
  [path]
  (if (coll? path)
    (->> path
      (map parse-coll))
    [(parse-coll path)]))


(defn resource-path
  "Returns a java.io.File object with the parsed `path` applied to it.
   `path` is a scalar or a vector."
  [path]
  (apply io/file db-dir (resolve-path path)))

(defn make-vec [x]
  (if (coll? x)
    x
    [x]))

(defn get-resource
  "Takes the name of the collection and, optionally, the id (name) of the 
   document.
   Returns the an io/file if the resource exists, else returns nil."
  ([cname]
   (get-resource cname nil))
  ([cname doc-id]
   (let [cname (make-vec cname)
         params (if doc-id
                  (conj cname doc-id)
                  cname)
         file (resource-path params)]
     (when (fs/exists? file)
       file))))

(defn get-doc-file
  "Returns the file where the document is stored."
  ([coll]
   (let [file (io/file (resource-path coll) "data.edn")]
     (when (fs/exists? file)
       file)))
  ([coll id]
   (let [file (io/file (resource-path coll) (str id) "data.edn")]
     (when (fs/exists? file)
       file))))

(defn use-qualified-keywords?
  "Returns true if the settings file has the :use-qualified-keywords? 
   set to `true` It's `false` by default."
  []
  (:use-qualified-keywords? @settings))


(defn get-collection-key
  "Returns the keyword to be used in the collection's document.
   `cname` is the identity of the collection and can be a scalar or a vector.
   `field` is the field name and must be a keyword or string"
  [cname field]
  (if (use-qualified-keywords?)
    (keyword (-> (if (coll? cname)
                   (last cname)
                   cname)
               name)
      (name field))
    field))

(defn get-id
  [coll data]
  (get data
    (get-collection-key coll :id)))

(defn now
  "Returns the current timestamp."
  []
  (java.time.Instant/now))


;;; ----------------------------------------------------------------------------
;;; Settings
;;; ----------------------------------------------------------------------------


; Management of id increment

(def settings
  "DB settings."
  (atom {}))


(defn load-settings! []
  (reset! settings (load-edn settings-path)))


(defn save-settings!
  "Unlike regular documents, the settings file is saved with
   pretty print, to allow for better readbility."
  []
  (save-edn! settings-path @settings {:pretty-print? true}))


(defn next-id!
  "Returns a java.util.UUID object."
  []
  (str (java.util.UUID/randomUUID)))


(defn setup!
  "Checks if the db path and the settings file are set, otherwise will do it."
  [& [opts]]
  (when-not (fs/exists? db-dir)
    (fs/mkdirs db-dir))
  (when-not (fs/exists? settings-path)
    (let [opts (merge {;; use the collection name as the keyword ns
                       :use-qualified-keywords? false
                       ;; automatically add created-at and updated fields when 
                       ;; writing documents.
                       :add-timestamps? true}
                 opts)]
      (save-edn! settings-path opts)))
  (load-settings!))


(defn reset-db!
  "Deletes all data and settings."
  []
  (fs/delete-dir db-dir)
  (setup!))

(setup!)


;;; ----------------------------------------------------------------------------
;;; CREATE, DELETE TABLE
;;; ----------------------------------------------------------------------------

; TODO: remove
(defn create-coll!
  "Creates the settings for the collection and  dir where the documents will 
   be contained."
  [{:keys [coll]}]
  (if-let [path (get-resource coll)]
    (throw (Exception. (str "Collection already exists: " path)))
    (let [path (resource-path coll)
          config {:counter 0}]

      (prn path)
      (fs/mkdir path)

      (save-edn!
        settings-path
        (swap! settings update :collections assoc coll config)))))


(defn delete-coll!
  "Deletes all data and settings related to the given collection."
  [{:keys [coll]}]
  (let [path (get-resource coll)]
    (when (fs/exists? path)
      (fs/delete-dir path))))


(defn exists?
  [{:keys [coll]}]
  (fs/exists?
    (get-resource coll)))


;;; ----------------------------------------------------------------------------
;;; GET, SAVE, DELETE
;;; ----------------------------------------------------------------------------
;;; All files are expected to contain edn objects, so we just use
;;; clojure.edn/read when loading them from the file.


(defn get-by-id
  "Returns the contents of the document queried, if exists."
  [{:keys [coll id]}]
  (some-> (if id
            (get-doc-file coll id)
            (get-doc-file coll))
    load-edn))

(defn get-all
  "Reads and returns the contents of the given collection."
  [{:keys [coll]}]
  (some->> (get-resource coll)
    fs/list-dir
    (map fs/name)
    (map #(get-by-id {:coll coll :id %}))))


(defn order-by-helper [order-by-key documents]
  (case order-by-key
    :desc (reverse documents)
    :asc documents
    (sort-by order-by-key documents)))

;; TODO: test
(defn select
  "Returns a list of documents that match the given key/value pairs.
   
   (select 
     :users 
     {:where #(= (:name %) \"John\"),
      :offset 10, 
      :limit 10, 
      :order-by :age})}))"
  ([{:keys [coll where order-by offset limit]}]
   (let [result
         (some->> (get-all {:coll coll})
           where (filter where)
           order-by (order-by-helper order-by)
           offset (drop offset)
           limit (take limit))]
     (if (= limit 1)
       (first result)
       result))))

(defn maybe-add-created-at
  "Adds the created-at field to the data if it doesn't exist."
  [data k timestamp]
  (if (contains? data k)
    data
    (assoc data k timestamp)))

(defn add-updated-at
  "Adds the updated-at field to the data."
  [data k timestamp]
  (assoc data k timestamp))

(defn maybe-add-timestamps
  [data coll]
  (if-not (:add-timestamps? @settings)
    data
    (let [timestamp (now)]
      (-> data
        (maybe-add-created-at
          (get-collection-key coll :created-at) timestamp)
        (add-updated-at
          (get-collection-key coll :updated-at) timestamp)))))


(defn create-raw!
  "Creates a new document. `data` must contain the id of the document. 
   Returns `data` if successful."
  [{:keys [coll data]}]
  (assert (contains? data (get-collection-key coll :id))
    (str
      "You must provide an `id` key, or use `create!` to have it "
      "automatically generated."))

  (let [id (get-id coll data)
        path (io/file (resource-path coll) (str id))
        data (maybe-add-timestamps data coll)]

    (fs/mkdirs path)

    (save-edn! (io/file path "data.edn")
      data)))


(defn create!
  "Creates a new document. Returns the data with the id."
  [{:keys [coll data]}]
  (let [id (next-id!)
        data (assoc data
               (get-collection-key coll :id)
               id)]
    (create-raw! {:coll coll :data data})))


(defn where-id-file
  [{:keys [coll where]}]
  (let [id (get-id coll where)]
    [id, (get-doc-file coll id)]))

(defn coll-id-file
  [{:keys [coll]}]
  [nil, (get-doc-file coll)])


(defn update!
  "Updates the document for the given id, only for the keys given in `data`.
    If a document can't be found, returns nil.
    Takes a map with the `coll`, ?`where` clause, and the `data`.
    
   `opts` is a map with `save-mode` which is set to `:merge` by default,
   but can be set to `:set`, which will replace the whole document with
   the content of `data`."
  [{:keys [coll where data opts] :as params}]
  (let [save-mode (or (:save-mode opts) :merge)
        [id doc-file] (if where
                        (where-id-file params)
                        (coll-id-file params))]

    (when (fs/exists? doc-file)
      (let [data (maybe-add-timestamps data coll)
            old (get-by-id {:coll coll :id id})
            new (if (= save-mode :set)
                  data
                  (merge old data))]
        (save-edn! doc-file new)))))

;; (def coll [:users "c4aeb292-b001-4c43-8d14-d5aeede93bbb"
;;            :projects "7f8a9684-519b-4284-afd6-8651a12e7060"])
;; (update! {:coll coll
;;           :data {:name "updated"}})
;; (get-by-id {:coll coll})
(get-by-id {:coll ["tests" 1] :id nil})


(defn upsert!
  "Updates the document if it exists, otherwise creates it.
   Takes a map with the `coll`, ?`where` clause, and the `data`."
  [{:keys [_coll where _data _opts] :as params}]
  (let [[_id doc-file] (if where
                         (where-id-file params)
                         (coll-id-file params))]
    (if (fs/exists? doc-file)
      (update! params)
      (create! (select-keys params [:coll :data])))))


; TODO: test
(defn delete!
  "Deletes the document. If successful returns the document id. 
   If it doesn't exist, returns false."
  [{:keys [coll id]}]

  ;; delete the dir at id: /{db-dir}/{coll}/---->{id}/<----
  (if (some-> (io/file (resource-path coll) (str id))
        fs/delete-dir)
    id
    false))

#_(defn run-tests! []
    (let [coll "tests"
          data {:id 1
                :name "test 1"}
          doc2 (atom nil)]

      (when (fs/exists? (resource-path coll))
        (delete-coll! {:coll coll}))

      (swap! settings assoc :add-timestamps? false)

      ; create
      (println "create")
      (let [doc (create! {:coll coll :data {:name "test 2"}})]
        (reset! doc2 doc))
      (create-raw! {:coll coll :data data})

      ; get-by-id
      (println "get-by-id")
      (assert (= true (fs/exists? (get-doc-file coll 1))))
      (assert (= true (fs/exists? (get-doc-file coll (:id @doc2)))))
      (assert (= data (get-by-id {:coll [coll 1]})))
      (assert (= data (get-by-id {:coll coll :id 1})))
      (assert (= @doc2 (get-by-id {:coll [coll (:id @doc2)]})))

      ; get-all
      (println "get-all")
      (assert (= 2 (count (get-all {:coll coll}))))

      ; update
      (println "update")
      (update! {:coll [coll 1]
                :data {:name "updated"}})
      (assert (= {:id 1
                  :name "updated"}
                (get-by-id {:coll [coll 1]})))

      ; delete
      (println "delete")
      (delete! {:coll coll :id 1})
      (assert (= nil (get-doc-file coll 1)))
      (delete! {:coll [coll (:id @doc2)]})
      (assert (= 0 (count (get-all {:coll coll}))))
      (assert (= nil (get-by-id {:coll [coll 1]})))

      (delete-coll! {:coll coll})
      (assert (= false (fs/exists? (resource-path coll))))))

#_(run-tests!)

(comment

  ; basic API:

  ; all functions expect a map. a common key is :coll, which is the name of the
  ; collection. other keys are specific to each function.

  ; use create! and create-raw! to create documents inside collections.
  ; collections are created automatically when you create a document inside 
  ; them.

  ; use get-by-id to get a document by its id.
  ; use get-all to get all documents in a collection.
  ; use select to get documents with finer control, using the keys
  ; where, order-by, offset, limit.

  ; use update! to update a document.

  ; use delete-coll! to delete a collection.
  ; use delete! to delete a document.

  ; nested collections are supported, but you must use vectors to specify the
  ; path to the collection.
  ; example: [:users user-id :profiles]
  ; all other functions work the same.


  "tests:"


  (reset-db!)

  ; turn on qualified keywords (it's false by default)
  (swap! settings assoc :use-qualified-keywords? true)
  (save-settings!)

  (create! {:coll :users, :data {:users/name "Guest"}})
  (create! {:coll :users, :data {:users/name "Us3r"}})

  (get-all {:coll :users})

  (get-by-id {:coll :users, :id (-> (get-all {:coll :users})
                                  first
                                  :users/id)})




  (create-raw! {:coll :profile,
                :data {:profile/id "Guest"
                       :profile/dob "2023-01-01"}})

  (get-by-id {:coll :profile :id "Guest"})

  (update! {:coll :profile,
            :where {:profile/id "Guest"},
            :data {:profile/dob "2025-01-01"}})


  ; removing keys from db:
  (update! {:coll :profile,
            :where {:profile/id "Guest"},
            :data {:profile/id "Guest"}
            :opts {:save-mode :set}})

  ; delete a document:
  (delete! {:coll :profile :id "Guest"})

  ; delete a collection:
  (delete-coll! {:coll :profile})


  ; you can nest collections by your hearts content using vectors:
  (def user-id "47353bf7-7607-45f3-a46b-5a579fdd1e1d")
  (create-raw!
    {:coll [:users user-id :profiles]
     :data {:profiles/id "Guest"}})

  ; all other operations work the same:
  (get-by-id {:coll [:users user-id :profiles]
              :id "Guest"})

  (get-all {:coll [:users user-id :profiles]})

  (update! {:coll [:users user-id :profiles]
            :where {:profiles/id "Guest"}
            :data {:profiles/dob "2023-01-01"}})

  (delete! {:coll [:users user-id :profiles]}))