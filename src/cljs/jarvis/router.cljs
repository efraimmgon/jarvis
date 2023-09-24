(ns jarvis.router
  (:require
   [reitit.core :as reitit]
   [reitit.frontend.easy :as rfe]
   [re-frame.core :as rf]
   [jarvis.apps.dashboard.views :as dashboard]
   [jarvis.apps.projects.core :as projects]))


(defn page []
  (when-let [page @(rf/subscribe [:common/page])]
    [page]))


(defn navigate! [match _]
  (rf/dispatch [:common/navigate match]))


(def router
  (reitit/router
   [["/" {:name        :home
          :view        #'projects/all-projects-ui
          :controllers [{:start (fn [_]
                                  (rf/dispatch [:projects/load]))}]}]

    [projects/router]]))



(defn start-router! []
  (rfe/start!
   router
   navigate!
   {}))