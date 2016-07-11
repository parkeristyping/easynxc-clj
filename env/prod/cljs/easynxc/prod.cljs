(ns easynxc.prod
  (:require [easynxc.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
