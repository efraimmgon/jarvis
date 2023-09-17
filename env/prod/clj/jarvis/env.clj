(ns jarvis.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[jarvis started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[jarvis has shut down successfully]=-"))
   :middleware identity})
