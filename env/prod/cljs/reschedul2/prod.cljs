(ns reschedul2.app
  (:require [reschedul2.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
