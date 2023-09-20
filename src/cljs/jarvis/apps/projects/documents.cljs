(ns jarvis.apps.projects.documents
  (:require
   [reagent.core :as r]
   [re-frame.core :as rf]
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
;;; Main hanlders


(rf/reg-event-fx
 :projects.documents/create!
 base-interceptors
 (fn [_ [{:keys [project-id data]}]]
   (let [user (rf/subscribe [:identity])
         q (fsdb/create! {:coll [:users (:id @user) :projects project-id :documents]
                          :data data})]
     {:dispatch
      [:fsdb/query
       {:params q
        :on-success [:projects.documents/create-success]}]})))


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
 :projects.documents/update!
 base-interceptors
 (fn [_ [{:keys [project-id] :as params}]]
   (let [user (rf/subscribe [:identity])
         q (fsdb/update!
            (assoc (select-keys params [:where :data :opts])
                   :coll [:users (:id @user) :projects project-id :documents]))]
     {:dispatch
      [:fsdb/query
       {:params q
        :on-success [:projects.documents/update-success]}]})))


(rf/reg-event-fx
 :projects.documents/delete!
 base-interceptors
 (fn [_ [{:keys [project-id id]}]]
   (let [user (rf/subscribe [:identity])
         q (fsdb/delete! {:coll [:users (:id @user) :projects project-id :documents]
                          :id id})]
     {:dispatch
      [:fsdb/query
       {:params q
        :on-success [:projects.documents/delete-success]}]})))