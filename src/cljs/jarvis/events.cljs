(ns jarvis.events
  (:require
   [ajax.core :as ajax]
   [re-frame.core :as rf]
   [reitit.frontend.easy :as rfe]
   [reitit.frontend.controllers :as rfc]
   [jarvis.utils.events :refer [base-interceptors query]]
   jarvis.utils.fsdb))

;;; ---------------------------------------------------------------------------
;;; HANDLERS
;;; ---------------------------------------------------------------------------

;;; ---------------------------------------------------------------------------
;;; Misc

(rf/reg-event-db
 :assoc-in
 base-interceptors
 (fn [db [path v]]
   (assoc-in db path v)))


(rf/reg-event-db
 :update-in
 base-interceptors
 (fn [db [path f & args]]
   (apply update-in db path f args)))


(rf/reg-event-db
 :modal
 base-interceptors
 (fn [db [comp]]
   (js/window.scrollTo #js {"top" 0 "left" 0 "behavior" "smooth"})
   (let [modal-stack (:modal db)]
     (if (seq modal-stack)
       (update db :modal conj comp)
       (assoc db :modal [comp])))))


(rf/reg-event-db
 :remove-modal
 base-interceptors
 (fn [db _]
   (let [modal-stack (:modal db)]
     (if (seq modal-stack)
       (update db :modal pop)
       (assoc db :modal [])))))

(rf/reg-event-db
 :common/navigate
 (fn [db [_ match]]
   (let [old-match (:common/route db)
         new-match (assoc match :controllers
                          (rfc/apply-controllers (:controllers old-match) match))]
     (assoc db :common/route new-match))))

(rf/reg-fx
 :common/navigate-fx!
 (fn [[k & [params query]]]
   (rfe/push-state k params query)))

(rf/reg-event-fx
 :navigate!
 (fn [_ [_ url-key params query]]
   {:common/navigate-fx! [url-key params query]}))


(rf/reg-event-db
 :common/set-error
 base-interceptors
 (fn [db [error]]
   (assoc db :common/error error)))


(rf/reg-event-fx
 :common/log
 base-interceptors
 (fn [_ [msg]]
   (js/console.log msg)
   nil))


(rf/reg-event-db
 :initialize-db
 base-interceptors
 (fn [db _]
   (merge db
          {:jarvis/chat []
           :jarvis.chat/status :idle
           :identity {:active? true,
                      :updated-at #inst "2023-09-20T19:10:43.555558000-00:00",
                      :email "example@gmail.com",
                      :password "bcrypt+sha512$86186fc28f83b3e3db78bcf8350a3a57$12$8f215420e68fd7922561167b07354f05d8db6d49e212689e",
                      :username "admin",
                      :admin? true,
                      :last-login nil,
                      :id "c4aeb292-b001-4c43-8d14-d5aeede93bbb",
                      :created-at #inst "2023-09-20T19:10:43.553787000-00:00"}})))


(rf/reg-event-db
 :set-identity
 base-interceptors
 (fn [db [user]]
   (assoc db :identity user)))


;;; ---------------------------------------------------------------------------
;;; SUBSCRIPTIONS
;;; ---------------------------------------------------------------------------


(rf/reg-sub
 :common/page-id
 :<- [:common/route]
 (fn [route _]
   (-> route :data :name)))

(rf/reg-sub
 :common/page
 :<- [:common/route]
 (fn [route _]
   (-> route :data :view)))


(rf/reg-sub
 :query
 (fn [db [_ path]]
   (get-in db path)))

(rf/reg-sub :common/route query)
(rf/reg-sub :common/error query)
(rf/reg-sub :identity query)
(rf/reg-sub :modal query)