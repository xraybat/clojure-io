;; Excerpt from “Programming Clojure, Third Edition", Alex Miller, with Stuart Halloway, and Aaron Bedra
;; Ch. 7 Protocols and DataTypes, Sect. Protocols, pp. 511-513

(ns io.protocol
  (:import (java.io File FileInputStream InputStream InputStreamReader BufferedReader
                    FileOutputStream OutputStream OutputStreamWriter BufferedWriter)
           (java.net Socket URL)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; protocols are betterer
;; make-reader/-writer
(defprotocol IOFactory
  "a protocol for things that can be read from and written to"
  (make-reader [this] "creates a BufferedReader")
  (make-writer [this] "creates a BufferedWriter"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; read/write a file; new 'gulp' and 'expectorate'
(defn gulp [src]
  (let [sb (StringBuilder.)]
    (with-open [reader (make-reader src)]
      (loop [c (.read reader)]
        (if (neg? c)
          (str sb)
          (do
            (.append sb (char c))
            (recur (.read reader))))))))

(defn expectorate [dst content]
  (with-open [writer (make-writer dst)]
    (.write writer (str content))))

(extend-protocol IOFactory
  InputStream
  (make-reader [src]
    (-> src InputStreamReader. BufferedReader.))
  (make-writer [dst]
    (throw (IllegalArgumentException.
             "can't open as an InputStream")))

  OutputStream
  (make-reader [src]
    (throw (IllegalArgumentException.
             "can't open as an OutputStream")))
  (make-writer [dst]
    (-> dst OutputStreamWriter. BufferedWriter.))

  String                                                    ;; note: we need this too
  (make-reader [src]
    (make-reader (FileInputStream. src)))
  (make-writer [dst]
    (make-writer (FileOutputStream. dst)))

  File
  (make-reader [src]
    (make-reader (FileInputStream. src)))
  (make-writer [dst]
    (make-writer (FileOutputStream. dst)))

  Socket
  (make-reader [src]
    (make-reader (.getInputStream src)))
  (make-writer [dst]
    (make-writer (.getOutputStream dst)))

  URL
  (make-reader [src]
    (make-reader
      (if (= "file" (.getProtocol src))
        (-> src .getPath FileInputStream.)
        (.openStream src))))
  (make-writer [dst]
    (make-writer
      (if (= "file" (.getProtocol dst))
        (-> dst .getPath FileInputStream.)
        (throw (IllegalArgumentException.
                 "can't write to a non-file URL"))))))

;; test
(comment
  (expectorate "output-4.txt" (gulp "input.txt"))
)