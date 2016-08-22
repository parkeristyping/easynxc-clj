(ns easynxc.deleter
  (:require [chime :refer [chime-ch]]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
            [clojure.core.async :refer [<! go-loop]]
            [clj-time.periodic :refer [periodic-seq]]
            [clojure.java.io :as io]
            [taoensso.timbre :refer [info]]))

(defn is-uuid? [filename]
  (re-matches #"\w{8}-\w{4}-\w{4}-\w{4}-\w{12}\..+" filename))

(defn delete! [dir age]
  (map io/delete-file
       (filter (fn [f]
                 (and (is-uuid? (.getName f))
                      (< (.lastModified f) (- (c/to-long (t/now)) age))))
               (.listFiles (io/file dir)))))

(defn run-deleter [dir freq age]
  (let [chimes (chime-ch (periodic-seq (t/now)
                                       (-> freq t/seconds)))]
    (go-loop []
      (when-let [msg (<! chimes)]
        (info "Deleted " (count (delete! dir age)) " files")
        (recur)))))
