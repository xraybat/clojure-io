(ns io.cryptovault
  (:require [clojure.java.io :as io]
            [io.protocol :as protocol])
  (:import (java.security KeyStore KeyStore$SecretKeyEntry KeyStore$PasswordProtection)
           (javax.crypto Cipher KeyGenerator CipherOutputStream CipherInputStream)
           (java.io FileInputStream FileOutputStream)))

(defprotocol Vault
  (init-vault [vault])
  (vault-output-stream [vault])
  (vault-input-stream [vault]))

(defn vault-key [vault]
  (let [password (.toCharArray (.password vault))]
    (with-open [fis (FileInputStream. (.keystore vault))]
      (-> (doto (KeyStore/getInstance "JCEKS")
            (.load fis password))
          (.getKey "vault-key" password)))))

(deftype CryptoVault [filename keystore password]
  Vault
  (init-vault [vault]
    (let [password (.toCharArray (.password vault))
          key (.generateKey (KeyGenerator/getInstance "AES"))
          keystore (doto (KeyStore/getInstance "JCEKS")
                     (.load nil password)
                     (.setEntry "vault-key"
                                (KeyStore$SecretKeyEntry. key)
                                (KeyStore$PasswordProtection. password)))]
      (with-open [fos (FileOutputStream. (.keystore vault))]
        (.store keystore fos password))))

  (vault-output-stream [vault]
    (let [cipher (doto (Cipher/getInstance "AES")
                   (.init Cipher/ENCRYPT_MODE (vault-key vault)))]
      (CipherOutputStream. (io/output-stream (.filename vault)) cipher)))

  (vault-input-stream [vault]
    (let [cipher (doto (Cipher/getInstance "AES")
                   (.init Cipher/DECRYPT_MODE (vault-key vault)))]
      (CipherInputStream. (io/input-stream (.filename vault)) cipher)))

  protocol/IOFactory
  (make-reader [vault]
    (protocol/make-reader (vault-input-stream vault)))
  (make-writer [vault]
    (protocol/make-writer (vault-output-stream vault))))

(extend CryptoVault
  clojure.java.io/IOFactory
  (assoc io/default-streams-impl
    :make-input-stream (fn [x opts] (vault-input-stream x))
    :make-output-stream (fn [x opts] (vault-output-stream x))))

;; test
;;(comment
  (println (def vault (->CryptoVault "vault-file.dat" "keystore.dat" "toomanysecrets")))
  (println (init-vault vault))
  (println (protocol/expectorate vault "this is a test of CryptoVault"))
  (println (protocol/gulp vault))
;;)
