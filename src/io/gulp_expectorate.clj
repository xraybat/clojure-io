;; Excerpt from “Programming Clojure, Third Edition", Alex Miller, with Stuart Halloway, and Aaron Bedra
;; Ch. 7 Protocols and DataTypes, Sect. Programming to Abstractions, pp. 505-515

(ns io.gulp-expectorate
  (:import (java.io FileInputStream InputStreamReader BufferedReader
                    FileOutputStream OutputStreamWriter BufferedWriter)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; read/write a file; gulp, expectorate (-1)
(defn gulp [src]
  (let [sb (StringBuilder.)]
    (with-open [reader (-> src
                           FileInputStream.
                           InputStreamReader.
                           BufferedReader.)]
      (loop [c (.read reader)]
        (if (neg? c)
          (str sb)
          (do
            (.append sb (char c))
            (recur (.read reader))))))))

;; write to a file
(defn expectorate [dst content]
  (with-open [writer (-> dst
                         FileOutputStream.
                         OutputStreamWriter.
                         BufferedWriter.)]
    (.write writer (str content))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; makeReader / Writer (1)
(defn makeReader [src]
  (-> src FileInputStream. InputStreamReader. BufferedReader.))

(defn makeWriter [dst]
  (-> dst FileOutputStream. OutputStreamWriter. BufferedWriter.))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; read/write a file; gulp-, expectorate-2
(defn gulp-2 [src]
  (let [sb (StringBuilder.)]
    (with-open [reader (makeReader src)]
      (loop [c (.read reader)]
        (if (neg? c)
          (str sb)
          (do
            (.append sb (char c))
            (recur (.read reader))))))))

(defn expectorate-2 [dst content]
  (with-open [writer (makeWriter dst)]
    (.write writer (str content))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; makeReader/Writer3; there is no '2' just matching with 'gulp-3' and
;; 'expectorate-3'
(defn makeReader3 [src]
  (-> (condp = (type src)
        java.io.InputStream src
        java.lang.String (FileInputStream. src)
        java.io.File (FileInputStream. src)
        java.net.Socket (.getInputStream src)
        java.net.URL (if (= "file" (.getProtocol src))
                       (-> src .getPath FileInputStream.)
                       (.openStream src)))
      InputStreamReader.
      BufferedReader.))

(defn makeWriter3 [dst]
  (-> (condp = (type dst)
        java.io.OutputStream dst
        java.io.File (FileOutputStream. dst)
        java.lang.String (FileOutputStream. dst)
        java.net.Socket (.getOutputStream dst)
        java.net.URL (if (= "file" (.getProtocol dst))
                       (-> dst .getPath FileOutputStream.)
                       (throw (IllegalArgumentException. "can't write to non-file URL"))))
      OutputStreamWriter.
      BufferedWriter.))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; read/write a file; gulp-, expectorate-3
(defn gulp-3 [src]
  (let [sb (StringBuilder.)]
    (with-open [reader (makeReader3 src)]
      (loop [c (.read reader)]
        (if (neg? c)
          (str sb)
          (do
            (.append sb (char c))
            (recur (.read reader))))))))

;; write to a file
(defn expectorate-3 [dst content]
  (with-open [writer (makeWriter3 dst)]
    (.write writer (str content))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; interfaces
;; 'Any class that implements this interface must include make-reader and make-writer functions
;; that take a single parameter and an instance of the datatype itself and return a BufferedReader
;; and BufferedWriter, respectively.'
;;
;; but not an adequate clojure solution because java is determined at design time and can't be extended,
;; rather, use _protocols_

(comment
  ;; causes "clojure Interface methods must not contain '-'"
  ;;(definterface IOFactory
  ;;  (^java.io.BufferedReader make-reader [this])
  ;;  (^java.io.BufferedWriter make-writer [this]))

  ;; so, change all 'make-reader/-writer' above to 'makeReader/Writer'
  (definterface IOFactory
    (^java.io.BufferedReader makeReader [this])
    (^java.io.BufferedWriter makeWriter [this]))
)
