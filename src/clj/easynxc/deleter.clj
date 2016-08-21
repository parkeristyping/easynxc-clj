(ns easynxc.deleter
  (:require [chime :refer [chime-ch]]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
            [clojure.core.async :refer [<! go-loop]]
            [clj-time.periodic :refer [periodic-seq]]
            [clojure.java.io :as io]))

(defn is-uuid? [filename]
  (re-matches #"\w{8}-\w{4}-\w{4}-\w{4}-\w{12}\..+" filename))

(defn delete! [dir age]
  (let [files (.listFiles (io/file dir))
        cutoff (- (c/to-long (t/now)) age)]
    (map (fn [file]
           (let [last-modified (.lastModified file)
                 filename (.getName file)]
             (if (and
                  (< last-modified cutoff)
                  (is-uuid? filename))
               (do
                 (io/delete-file file)
                 (prn (str "Deleted: " filename))))))
         files)))

(defn run-deleter [dir freq age]
  (let [chimes (chime-ch (periodic-seq (t/now)
                                       (-> freq t/seconds)))]
    (go-loop []
      (when-let [msg (<! chimes)]
        (delete! dir age)
        (recur)))))
