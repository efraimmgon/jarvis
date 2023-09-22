(ns jarvis.apps.projects.documents
  (:require
   [reagent.core :as r]
   [re-frame.core :as rf]
   [jarvis.apps.embeddings :as embeddings]
   [jarvis.utils.events :refer [base-interceptors query]]
   [jarvis.utils.fsdb :as fsdb]))

;;; ---------------------------------------------------------------------------
;;; Handlers
;;; ---------------------------------------------------------------------------


;;; ---------------------------------------------------------------------------
;;; Response hanlders


(rf/reg-event-db
 :projects.documents/create-success
 base-interceptors
 (fn [db [document]]
   (update db :projects.documents/all conj document)))

(rf/reg-event-db
 :projects.documents/get-by-id-success
 base-interceptors
 (fn [db [document]]
   (assoc db :projects.documents/active document)))

(rf/reg-event-db
 :projects.documents/get-all-success
 base-interceptors
 (fn [db [documents]]
   (assoc db :projects.documents/all documents)))

(rf/reg-event-db
 :projects.documents/select-success
 base-interceptors
 (fn [db [documents]]
   (assoc db :projects.documents/all documents)))

(rf/reg-event-db
 :projects.documents/update-success
 base-interceptors
 (fn [db [document]]
   (assoc db :projects.documents/active document)))

(rf/reg-event-db
 :projects.documents/delete-success
 base-interceptors
 (fn [db [doc-id]]
   (update db :projects.documents/all
           #(remove (fn [p]
                      (= (:id p) doc-id))
                    %))))


;;; ---------------------------------------------------------------------------
;;; Helper hanlders 

(defn with-embedding!
  "Takes a document and returns a new document with the embedding assoced."
  [{:keys [data on-success]}]
  (let [p>response (embeddings/create-embedding!
                    (str (:title data) "\n\n" (:content data)))]
    (-> p>response
        (.then (fn [response]
                 (println "Tokens:" (embeddings/get-tokens response))
                 (on-success
                  (assoc data :embedding (embeddings/get-embedding response)))))

        (.catch prn))))



;;; ---------------------------------------------------------------------------
;;; Main hanlders

(rf/reg-event-fx
 :projects.documents/create-raw-with-embedding!
 base-interceptors
 (fn [{:keys [db]} [{:keys [project-id user-id] :as data} emb-resp]]
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
 :projects.documents/create-raw!
 base-interceptors
 (fn [_ [data]]
   {:dispatch
    [:create-embedding
     {:input (str (:title data) "\n\n" (:content data))
      :on-success [:projects.documents/create-raw-with-embedding! data]}]}))



(rf/reg-event-fx
 :projects.documents/get-by-id
 base-interceptors
 (fn [_ [{:keys [project-id id]}]]
   (let [user (rf/subscribe [:identity])
         q (fsdb/get-by-id {:coll [:users (:id @user) :projects project-id :documents]
                            :id id})]
     {:dispatch
      [:fsdb/query
       {:params q
        :on-success [:projects.documents/get-by-id-success]}]})))


(rf/reg-event-fx
 :projects.documents/get-all
 base-interceptors
 (fn [_ [{:keys [project-id]}]]
   (let [user (rf/subscribe [:identity])
         q (fsdb/get-all {:coll [:users (:id @user) :projects project-id :documents]})]
     {:dispatch
      [:fsdb/query
       {:params q
        :on-success [:projects.documents/get-all-success]}]})))


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
 (fn [_ [{:keys [data] :as params} resp-emb]]
   (let [{:keys [project-id id user-id]} data
         embedding (embeddings/get-embedding resp-emb)
         q (fsdb/update!
            (-> params
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
 (fn [_ [{:keys [data] :as params}]]
   {:dispatch
    [:create-embedding
     {:input (str (:title data) "\n\n" (:content data))
      :on-success [:projects.documents/update-with-embedding! params]}]}))


(rf/reg-event-fx
 :projects.documents/delete!
 base-interceptors
 (fn [_ [{:keys [project-id id]}]]
   (let [user (rf/subscribe [:identity])
         q (fsdb/delete! {:coll [:users (:id @user) :projects project-id :documents]
                          :id id})]
     {:dispatch-n
      [:fsdb/query
       {:params q
        :on-success [:projects.documents/delete-success]}]})))


;;; ---------------------------------------------------------------------------
;;; Subs


(rf/reg-sub :projects.documents/all query)
(rf/reg-sub :projects.documents/active query)