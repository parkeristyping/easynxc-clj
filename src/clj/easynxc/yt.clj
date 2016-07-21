(ns easynxc.yt
  (:require [clojure.java.shell :refer [sh]]
            [clojure.data.json :as json]
            [clojure.walk :refer [keywordize-keys]]))

(defn find-audio [formats]
  (let [audio-format (some #(when (and (= "none" (% :vcodec)) (some #{(% :ext)} ["m4a" "mp3"])) %) formats)]
    (if audio-format (audio-format :format_id))))

(defn get-formats [metadata-resp]
  (:formats (keywordize-keys (json/read-str (:out metadata-resp)))))

(defn get-destination [out]
  (last (re-find #"Destination: (.+)\n" out)))

(defn download [url]
  (let [metadata-resp (sh "youtube-dl" "--no-playlist" "-j" url)]
    (if (not= (metadata-resp :err) "")
      {:err (metadata-resp :err)}
      (let [audio-format (find-audio (get-formats metadata-resp))
            uuid (java.util.UUID/randomUUID)
            output-format (str "/tmp/" uuid ".%(ext)s")
            out ((if audio-format
                   (sh "youtube-dl" url "--no-playlist" "-f" audio-format "-o" output-format)
                   (sh "youtube-dl" url "--no-playlist" "-x" "--audio-format" "mp3" "-k" "-o" output-format)) :out)
            destination (get-destination out)]
        (if destination
          {:filename destination}
          {:err out})))))
