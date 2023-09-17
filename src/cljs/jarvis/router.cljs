(ns jarvis.router
  (:require
   [reitit.core :as reitit]
   [reitit.frontend.easy :as rfe]
   [re-frame.core :as rf]
   [jarvis.apps.dashboard.views :as dashboard]))


(defn page []
  (when-let [page @(rf/subscribe [:common/page])]
    [page]))


(defn navigate! [match _]
  (rf/dispatch [:common/navigate match]))


(def router
  (reitit/router
   [["/" {:name        :home
          :view        #'dashboard/dashboard-ui}]]))


(defn start-router! []
  (rfe/start!
   router
   navigate!
   {}))