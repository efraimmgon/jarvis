(ns jarvis.apps.projects.new-project
  (:require
   [reagent.core :as r]
   [jarvis.apps.projects.common :refer [project-ui]]))

(defn new-project-ui []
  (let [project (r/atom nil)]

    (set! (.-title js/document) "New Project")

    (fn []
      [project-ui project])))
