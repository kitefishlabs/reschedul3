(ns sand-compass.app
  (:require [sand-compass.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
