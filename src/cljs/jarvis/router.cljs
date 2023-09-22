(ns jarvis.router
  (:require
   [reitit.core :as reitit]
   [reitit.frontend.easy :as rfe]
   [re-frame.core :as rf]
   [jarvis.apps.dashboard.views :as dashboard]
   [jarvis.apps.projects.core :as projects]
   [jarvis.apps.projects.documents :as documents]))


(defn page []
  (when-let [page @(rf/subscribe [:common/page])]
    [page]))


(defn navigate! [match _]
  (rf/dispatch [:common/navigate match]))


(def router
  (reitit/router
   [["/" {:name        :home
          :view        #'projects/all-projects-ui}]
    ["/projects"
     ["/new"
      {:name :projects/new
       :view #'dashboard/dashboard-ui}]]]))


(defn start-router! []
  (rfe/start!
   router
   navigate!
   {}))