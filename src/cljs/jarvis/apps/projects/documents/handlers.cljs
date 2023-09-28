(ns jarvis.apps.projects.documents.handlers
  (:require
    [re-frame.core :as rf]
    [jarvis.apps.embeddings :as embeddings]
    [jarvis.utils.events :refer [base-interceptors query]]
    [jarvis.utils.fsdb :as fsdb]))

;;; ---------------------------------------------------------------------------
;;; Handlers
;;; ---------------------------------------------------------------------------

;;; ---------------------------------------------------------------------------
;;; Response handlers


(rf/reg-event-fx
  :projects.documents/create-success
  base-interceptors
  (fn [{:keys [db]} [{:keys [project-id] :as document}]]
    {:db (update db :projects.documents/list conj document)
     :dispatch [:navigate! :projects.documents/list
                {:project-id project-id}]}))

(rf/reg-event-db
  :projects.documents/load-by-id-success
  base-interceptors
  (fn [db [document]]
    (assoc db :projects.documents/active document)))

(rf/reg-event-db
  :projects.documents/load-success
  base-interceptors
  (fn [db [documents]]
    (assoc db :projects.documents/list documents)))

(rf/reg-event-db
  :projects.documents/select-success
  base-interceptors
  (fn [db [documents]]
    (assoc db :projects.documents/list documents)))

(rf/reg-event-fx
  :projects.documents/update-success
  base-interceptors
  (fn [{:keys [db]} [{:keys [project-id document-id] :as document}]]
    {:db (assoc db :projects.documents/active document)
     :dispatch [:navigate! :projects.documents/list
                {:project-id project-id
                 :document-id document-id}]}))
    

(rf/reg-event-fx
  :projects.documents/delete-success
  base-interceptors
  (fn [{:keys [db]} [project-id doc-id]]
    {:db (update db :projects.documents/list
           #(remove (fn [doc]
                      (= (:id doc) doc-id))
              %))
     :dispatch [:navigate! :projects.documents/list
                {:project-id project-id}]}))

    


;;; ---------------------------------------------------------------------------
;;; Helper hanlders 


(defn with-embedding!
  "Takes a document and returns a new document with the embedding assoced."
  [{:keys [data on-success]}]
  (let [response+ (embeddings/create-embedding!
                    (str (:name data) "\n\n" (:content data)))]
    (-> response+
      (.then (fn [response]
               (println "Tokens:" (embeddings/get-tokens response))
               (on-success
                 (assoc data :embedding (embeddings/get-embedding response)))))

      (.catch prn))))

(rf/reg-event-db
  :projects.documents/set-list
  base-interceptors
  (fn [db [documents]]
    (assoc db :projects.documents/list documents)))

(rf/reg-event-db
  :projects.documents/set-active
  base-interceptors
  (fn [db [document]]
    (assoc db :projects.documents/active document)))


;;; ---------------------------------------------------------------------------
;;; Main hanlders

(rf/reg-event-fx
  :projects.documents/create-with-embedding!
  base-interceptors
  (fn [_ [{:keys [project-id user-id] :as data} emb-resp]]
    (let [doc-id (str (random-uuid))
          embedding (embeddings/get-embedding emb-resp)
          q (fsdb/create-raw!
              {:coll [:users user-id :projects project-id :documents]
               :data (assoc data
                       :id doc-id
                       :embedding embedding)})]

      (println "tokens:" (embeddings/get-tokens emb-resp))

      {:dispatch-n
       [[:fsdb/query
         {:params q
          :on-success [:projects.documents/create-success]}]
        [:fsdb/query
         {:params (fsdb/create-raw!
                    {:coll [:users user-id :embeddings],
                     :data {:id (str project-id "_" doc-id)
                            :project-id project-id
                            :document-id doc-id
                            :embedding embedding}})}]]})))


(rf/reg-event-fx
  :projects.documents/create!
  base-interceptors
  (fn [_ [data]]
    {:dispatch
     [:create-embedding
      {:input (str (:name data) "\n\n" (:content data))
       :on-success [:projects.documents/create-with-embedding! data]}]}))



(rf/reg-event-fx
  :projects.documents/load-by-id
  base-interceptors
  (fn [_ [project-id id]]
    (let [user (rf/subscribe [:identity])
          q (fsdb/get-by-id {:coll [:users (:id @user) 
                                    :projects project-id 
                                    :documents id]})]

      {:dispatch
       [:fsdb/query
        {:params q
         :on-success [:projects.documents/load-by-id-success]}]})))


(rf/reg-event-fx
  :projects.documents/load-list
  base-interceptors
  (fn [_ [project-id]]
    (let [user (rf/subscribe [:identity])
          q (fsdb/get-all {:coll [:users (:id @user)
                                  :projects project-id
                                  :documents]})]
      {:dispatch
       [:fsdb/query
        {:params q
         :on-success [:projects.documents/load-success]}]})))


(rf/reg-event-fx
  :projects.documents/select
  base-interceptors
  (fn [_ [{:keys [project-id _where _order-by _offset _limit] :as params}]]
    (let [user (rf/subscribe [:identity])
          q (fsdb/select
              (assoc (select-keys params [:where :order-by :offset :limit])
                :coll [:users (:id @user) :projects project-id :documents]))]
      {:dispatch
       [:fsdb/query
        {:params q
         :on-success [:projects.documents/select-success]}]})))


(rf/reg-event-fx
  :projects.documents/update-with-embedding!
  base-interceptors
  (fn [_ [data resp-emb]]
    (let [{:keys [project-id id user-id]} data
          embedding (embeddings/get-embedding resp-emb)
          q (fsdb/update!
              (-> {:data data}
                (assoc :coll [:users user-id :projects project-id :documents id])
                (assoc-in [:data :embedding] embedding)))
          embedding-id (str project-id "_" id)]

      {:dispatch-n
       [[:fsdb/query
         {:params q
          :on-success [:projects.documents/update-success]}]
        [:fsdb/query
         {:params (fsdb/update!
                    {:coll [:users user-id :embeddings embedding-id]
                     :data {:embedding embedding}})}]]})))


(rf/reg-event-fx
  :projects.documents/update!
  base-interceptors
  (fn [_ [data]]
    {:dispatch
     [:create-embedding
      {:input (str (:name data) "\n\n" (:content data))
       :on-success [:projects.documents/update-with-embedding! data]}]}))


(rf/reg-event-fx
  :projects.documents/delete!
  base-interceptors
  (fn [_ [{:keys [project-id user-id id]}]]
    (let [q (fsdb/delete! {:coll [:users user-id 
                                  :projects project-id 
                                  :documents id]})]
      {:dispatch
       [:fsdb/query
        {:params q
         :on-success [:projects.documents/delete-success project-id]}]})))


;;; ---------------------------------------------------------------------------
;;; Subs


(rf/reg-sub :projects.documents/list query)
(rf/reg-sub :projects.documents/active query)