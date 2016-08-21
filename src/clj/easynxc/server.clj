(ns easynxc.server
  (:require [easynxc.handler :refer [app]]
            [easynxc.deleter :refer [run-deleter]]
            [config.core :refer [env]]
            [ring.adapter.jetty :refer [run-jetty]])
  (:gen-class))

(defn -main [& args]
  (let [port (Integer/parseInt (or (env :port) "8080"))]
    (do
      ;; start a scheduled job to regularly delete old tmp files
      (run-deleter "/tmp" 300 300)
      ;; run the server
      (run-jetty app {:port port :join? false}))))
