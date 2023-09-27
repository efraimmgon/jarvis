(ns jarvis.apps.projects.handlers
  (:require [jarvis.utils.events :refer [<sub base-interceptors query]]
            [jarvis.utils.fsdb :as fsdb]
            [re-frame.core :as rf]))


;;; ---------------------------------------------------------------------------
;;; Handlers
;;; ---------------------------------------------------------------------------


;;; ---------------------------------------------------------------------------
;;; Utils


(defn prepare-project
  [{:keys [user-id] :as project}]
  (cond-> project
    (not user-id) (assoc :user-id (:id (<sub [:identity])))))

(rf/reg-event-db
 :projects/set-active
 base-interceptors
 (fn [db [project]]
   (assoc db :projects/active project)))

(rf/reg-event-db
 :projects/set
 base-interceptors
 (fn [db [projects]]
   (assoc db :projects/all projects)))


;;; ---------------------------------------------------------------------------
;;; Response hanlders


(rf/reg-event-fx
 :projects/create-success
 base-interceptors
 (fn [{:keys [db]} [project]]
   {:db (update db :projects/all conj project)
    :dispatch [:navigate! :projects/documents {:project-id (:id project)}]}))

(rf/reg-event-fx
 :projects/update-success
 base-interceptors
 (fn [{:keys [db]} [project]]
   {:db (update db :projects/all
                #(mapv (fn [p]
                         (if (= (:id p) (:id project))
                           project
                           p))
                       %))
    :dispatch [:navigate! :home]}))

(rf/reg-event-fx
 :projects/delete-success
 base-interceptors
 (fn [{:keys [db]} [project-id]]
   {:db (update db :projects/all
                #(remove (fn [p]
                           (= (:id p) project-id))
                         %))
    :dispatch [:navigate! :home]}))


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
        :on-success [:projects/set]}]})))


(rf/reg-event-fx
 :projects/load-by-id
 base-interceptors
 (fn [_ [project-id]]
   (let [user (rf/subscribe [:identity])
         q (fsdb/get-by-id {:coll [:users (:id @user) :projects project-id]})]
     {:dispatch
      [:fsdb/query
       {:params q
        :on-success [:projects/set-active]}]})))


(rf/reg-event-fx
 :projects/create!
 base-interceptors
 (fn [_ [data]]
   (let [user (rf/subscribe [:identity])
         q (fsdb/create! {:coll [:users (:id @user) :projects]
                          :data (prepare-project data)})]
     {:dispatch
      [:fsdb/query
       {:params q
        :on-success [:projects/create-success]}]})))




(rf/reg-event-fx
 :projects/update!
 base-interceptors
 (fn [_ [{:keys [user-id id] :as params}]]
   (let [q (fsdb/update! {:coll [:users user-id :projects id]
                          :data (select-keys params
                                             [:name :description :boosted?])})]
     {:dispatch
      [:fsdb/query
       {:params q
        :on-success [:projects/update-success]}]})))


(rf/reg-event-fx
 :projects/delete!
 base-interceptors
 (fn [_ [{:keys [id user-id]}]]
   (let [q (fsdb/delete! {:coll [:users user-id :projects id]})]
     {:dispatch
      [:fsdb/query
       {:params q
        :on-success [:projects/delete-success]}]})))


;; subscriptions

(rf/reg-sub :projects/all query)
(rf/reg-sub :projects/active query)