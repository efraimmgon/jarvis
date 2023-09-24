(ns jarvis.apps.projects.handlers
  (:require [jarvis.utils.events :refer [base-interceptors query]]
            [jarvis.utils.fsdb :as fsdb]
            [re-frame.core :as rf]))


;;; ---------------------------------------------------------------------------
;;; Handlers
;;; ---------------------------------------------------------------------------

;;; ---------------------------------------------------------------------------
;;; Response hanlders

(rf/reg-event-db
 :projects/load-success
 base-interceptors
 (fn [db [projects]]
   (assoc db :projects/all projects)))

(rf/reg-event-db
 :projects/load-by-id-success
 base-interceptors
 (fn [db [project]]
   (assoc db :projects/active project)))

(rf/reg-event-fx
 :projects/create-success
 base-interceptors
 (fn [{:keys [db]} [project]]
   {:db (update db :projects/all conj project)
    :dispatch [:navigate! :projects/documents {:project-id (:id project)}]}))

(rf/reg-event-db
 :projects/update-project-success
 base-interceptors
 (fn [db [project]]
   (assoc db :projects/active project)))

(rf/reg-event-db
 :projects/delete-project-success
 base-interceptors
 (fn [db [project-id]]
   (update db :projects/all
           #(remove (fn [p]
                      (= (:id p) project-id))
                    %))))

;;; ---------------------------------------------------------------------------
;;; Main handlers


(rf/reg-event-fx
 :projects/load
 base-interceptors
 (fn [_ _]
   (let [user (rf/subscribe [:identity])
         q (fsdb/get-all {:coll [:users (:id @user) :projects]})]
     {:dispatch
      [:fsdb/query
       {:params q
        :on-success [:projects/load-success]}]})))


(rf/reg-event-fx
 :projects/load-by-id
 base-interceptors
 (fn [_ [project-id]]
   (let [user (rf/subscribe [:identity])
         q (fsdb/get-by-id {:coll [:users (:id @user) :projects project-id]})]
     {:dispatch
      [:fsdb/query
       {:params q
        :on-success [:projects/load-by-id-success]}]})))

(rf/reg-event-fx
 :projects/create!
 base-interceptors
 (fn [_ [{:keys [data]}]]
   (let [user (rf/subscribe [:identity])
         q (fsdb/create! {:coll [:users (:id @user) :projects]
                          :data data})]
     {:dispatch
      [:fsdb/query
       {:params q
        :on-success [:projects/create-success]}]})))


(rf/reg-event-fx
 :projects/update-project!
 base-interceptors
 (fn [_ [params]]
   (let [user (rf/subscribe [:identity])
         q (fsdb/update!
            (assoc (select-keys params [:where :data :opts])
                   :coll [:users (:id @user) :projects]))]
     {:dispatch
      [:fsdb/query
       {:params q
        :on-success [:projects/update-project-success]}]})))


(rf/reg-event-fx
 :projects/delete-project!
 base-interceptors
 (fn [_ [{:keys [id]}]]
   (let [user (rf/subscribe [:identity])
         q (fsdb/delete! {:coll [:users (:id @user) :projects]
                          :id id})]
     {:dispatch
      [:fsdb/query
       {:params q
        :on-success [:projects/delete-project-success]}]})))


;; subscriptions

(rf/reg-sub :projects/all query)
(rf/reg-sub :projects/active query)