(ns fc4c.integrations.structurizr.express.spec
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.string :as str :refer [blank?]]
            [com.gfredericks.test.chuck.generators :refer [string-from-regex]]
            [fc4c.spec :as fs]))

(defn- ns-with-alias
  [ns-sym alias-sym]
  (create-ns ns-sym)
  (alias alias-sym ns-sym))

(def ^:private aliases-to-namespaces
  {'st 'structurizr
   'sc 'structurizr.container
   'se 'structurizr.element
   'sr 'structurizr.relationship
   'ss 'structurizr.style
   'sd 'structurizr.diagram})

(doseq [[alias-sym ns-sym] aliases-to-namespaces]
  (ns-with-alias ns-sym alias-sym))

(s/def ::st/name ::fs/non-blank-simple-str)
(s/def ::st/description ::fs/non-blank-simple-str)

(s/def ::st/tags ::fs/non-blank-simple-str) ;; comma-delimited TODO: use a regex

(s/def ::st/position ::fs/coord-string)

(s/def ::st/foo string?)

(def int-pattern #"\d{1,4}")
(s/def ::st/int-in-string
  (s/with-gen (s/and string? (partial re-matches int-pattern))
    #(string-from-regex int-pattern)))

;;;; Elements

(s/def ::sc/type #{"Container"})
(s/def ::sc/technology string?)

(s/def ::st/container
  (s/keys :req-un [::st/name ::st/position ::sc/type]
          :opt-un [::st/description ::st/tags ::sc/technology]))

(s/def ::se/type #{"Person" "Software System"})
(s/def ::se/containers (s/coll-of ::st/container :min-count 1))

(s/def ::st/element
  (s/keys :req-un [::st/name ::st/position ::se/type]
          :opt-un [::st/description ::st/tags ::se/containers]))

;;;; Relationships

(s/def ::sr/source ::st/name)
(s/def ::sr/destination ::st/name)
(s/def ::sr/order ::st/int-in-string)
(s/def ::sr/vertices (s/coll-of ::st/position :min-count 1))

(s/def ::st/relationship
  (s/keys :req-un [::sr/source ::sr/destination]
          :opt-un [::st/description ::st/tags ::sr/vertices ::sr/order]))

;;;; Styles

(s/def ::ss/type #{"element" "relationship"})
(s/def ::ss/tag ::fs/non-blank-simple-str)
(s/def ::ss/width ::fs/coord-int)
(s/def ::ss/height ::fs/coord-int)
(s/def ::ss/color ::fs/non-blank-simple-str) ;;; TODO: Make this more specific
(s/def ::ss/shape #{"Box" "RoundedBox" "Circle" "Ellipse" "Hexagon" "Person"
                    "Folder" "Cylinder" "Pipe"})
(s/def ::ss/background ::fs/non-blank-simple-str) ;;; TODO: Make this more specific
(s/def ::ss/dashed #{"true" "false"})
(s/def ::ss/border #{"Dashed" "Solid"})

(s/def ::st/style
  (s/keys :req-un [::ss/type ::ss/tag]
          :opt-un [::ss/color ::ss/shape ::ss/background ::ss/dashed ::ss/border
                   ::ss/width ::ss/height]))

;;;; Diagrams

(s/def ::sd/type #{"System Landscape" "System Context" "Container"})
(s/def ::sd/scope ::st/name)
(s/def ::sd/size #{"A2_Landscape" "A3_Landscape"}) ;;; TODO: Add the rest of the options
(s/def ::sd/elements (s/coll-of ::st/element :min-count 1))
(s/def ::sd/relationships (s/coll-of ::st/relationship :min-count 1))
(s/def ::sd/styles (s/coll-of ::st/style :min-count 1))

(s/def ::st/diagram
  (s/keys :req-un [::sd/type ::sd/scope ::sd/elements ::sd/relationships
                   ::sd/styles ::sd/size]
          :opt-un [::st/description]))
