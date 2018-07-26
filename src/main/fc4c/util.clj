(ns fc4c.util
  (:require [clojure.walk        :as walk  :refer [postwalk]]
            [clojure.spec.alpha  :as s]
            [fc4c.spec           :as fs]))

;; TODO: consider making this a macro so the ns-symbols won’t have to be quoted
;; when calling them.
(defn namespaces
  "Pass one or more tuples of namespaces to create along with aliases:
  (namespaces '[foo :as f] '[bar :as b])"
  [t & ts] ; At least one tuple is required.
  ;; TODO: is it silly to use all these preconditions when I already have a spec?
  {:pre [(every? indexed? (concat [t] ts))
         (every? #(= (count %) 3) (concat [t] ts))
         (every? #(= (second %) :as) (concat [t] ts))
         (every? #(every? symbol? (take-nth 2 %)) (concat [t] ts))]}
  (doseq [[ns-sym _ alias-sym] (concat [t] ts)]
    (create-ns ns-sym)
    (alias alias-sym ns-sym)))

; This spec is here for documentation and instrumentation; don’t do any
; generative testing with this spec because this function has side effects (and
; is mutating the state of the current namespace, and can thus fail in all sorts
; of odd ways).
(s/fdef namespaces
        :args (s/cat :args (s/+ (s/tuple simple-symbol? #{:as} simple-symbol?)))
        :ret  nil?)

(defn lookup-table-by
  "Given a function and a seqable, returns a map of (f x) to x.

  For example:
  => (lookup-table-by :name [{:name :foo} {:name :bar}])
  {:foo {:name :foo}, :bar {:name :bar}}"
  [f xs]
  (zipmap (map f xs) xs))

(defn add-ns
  [namespace keeword]
  (keyword (name namespace) (name keeword)))

(s/def ::keyword-or-simple-string
  (s/or :keyword keyword?
        :string  ::fs/non-blank-simple-str))

(s/fdef add-ns
        :args (s/cat :namespace ::keyword-or-simple-string
                     :keyword   ::keyword-or-simple-string)
        :ret  qualified-keyword?)

(defn update-all
  "Given a map and a function of entry (coll of two elems) to entry, applies the
  function recursively to every entry in the map."
  {:fork-of 'clojure.walk/stringify-keys}
  [f m]
  (postwalk
   (fn [x]
     (if (map? x)
       (into {} (map f x))
       x))
   m))

(s/def ::map-entry
  (s/tuple any? any?))

(s/fdef update-all
        :args (s/cat :fn (s/fspec :args (s/cat :entry ::map-entry)
                                  :ret  ::map-entry)
                     :map map?)
        :ret  map?)

(defn qualify-keys
  "Given a nested map with keyword keys, qualifies all keys, recursively, with
  the current namespace."
  [m ns-name]
  (update-all
   (fn [[k v]]
     (if (and (keyword? k)
              (not (qualified-keyword? k)))
       [(add-ns ns-name k) v]
       [k v]))
   m))

(s/fdef qualify-keys
        :args (s/cat :map (s/map-of keyword? any?)
                     :ns-name ::fs/non-blank-simple-str)
        :ret  (s/map-of qualified-keyword? any?))
