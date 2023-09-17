(ns jarvis.core
  (:require
   [day8.re-frame.http-fx]
   [reagent.dom :as rdom]
   [re-frame.core :as rf]
   [goog.events]
   [goog.history.EventType]
   [jarvis.ajax :as ajax]
   [jarvis.events]
   [jarvis.openai]
   [jarvis.router :as router])
  (:import goog.History))


;;; ---------------------------------------------------------------------------
;;; Initialize app
;;; ---------------------------------------------------------------------------


(defn ^:dev/after-load mount-components []
  (rf/clear-subscription-cache!)
  (rdom/render [#'router/page] (.getElementById js/document "app")))


(defn init! []
  (rf/dispatch-sync [:initialize-db])
  (router/start-router!)
  (ajax/load-interceptors!)
  (mount-components))