(ns easynxc.middleware
  (:require [ring.middleware.defaults :refer [site-defaults wrap-defaults]]))

(defn wrap-middleware [handler]
  (wrap-defaults handler (assoc-in site-defaults [:security :anti-forgery] false)))
