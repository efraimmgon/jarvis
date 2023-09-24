(ns jarvis.apps.projects.edit-project
  (:require
   [re-frame.core :as rf]
   [reagent.core :as r]
   [jarvis.apps.projects.common :refer [project-ui]]))


(defn edit-project-ui []
  (let [project- (rf/subscribe [:projects/active])]
    (set! (.-title js/document) "Edit Project")
    (fn []
      (when @project-
        (let [project (r/atom @project-)]
          [project-ui project])))))