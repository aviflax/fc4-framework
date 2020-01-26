(ns fc4.renderers.dali
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [cognitect.anomalies :as anom]
            [dali.io :as dio]
            [dali.layout.align] ; for side-fx
            [dali.layout.stack] ; for side-fx
            [dali.prefab :as dp]
            [dali.syntax :as ds]
            [fc4.io.dsl :as fid]
            [fc4.rendering :as r]
            [fc4.util :as u]))

(u/namespaces '[fc4 :as f]
              '[fc4.view :as v])

(def canvas-sizes
  "Map of canvas sizes to dimensions."
  {"A4_Landscape" [600 400]})

(defn- text-stack [texts]
  (into [:dali/stack {:direction :down :gap 6}]
        (map #(vector :text {:font-family "Fira Sans Light" :font-weight "light" :font-size 16} %) texts)))

(defn- element
  [id text cl pos]
   [:g {}
    [:dali/align {:axis :center}
     [:rect {:id id :class [cl :box-text] :filter "url(#ds)" :fill "#bf9af2" :stroke "#777777"}
            pos
            [120 80]
            10]
     (text-stack (str/split-lines text))]])

(defn- elements
  [view _model]
  ; [(element "A thing" "Marketplace" :system [10 10])]
  ; (clojure.pprint/pprint (get-in view [::v/positions ::v/containers]))
  (mapv (fn [[elem-name pos]] (element elem-name elem-name :system (vec pos)))
        (get-in view [::v/positions ::v/containers])))

(defn render
  "Renders an FC4 view on an FC4 model. Returns either an fc4.rendering/failure-result (which is a
  cognitect.anomalies/anomaly) or an fc4.rendering/success-result."
  [view model _opts]
  (into [:dali/page
          [:defs (dp/drop-shadow-effect :ds {:opacity 0.3 :offset [5 5] :radius 6})]]
    (elements view model)))

(s/def ::options map?)

(s/fdef render
  :args (s/cat :view  ::f/view
               :model ::f/model
               :opts  ::options)
  :ret  (s/or :success ::r/success-result
              :failure ::r/failure-result))


(comment

  ; (require '[dali.io :as dio])

  (defn tr [doc]
    (dio/render-svg doc "test.svg")
    (dio/render-png doc "test.png"))

  (let [v (fid/read-view "test/data/views/middle (valid).yaml")
        m (fid/read-model "test/data/models/valid/a/flat")
        d (render v m nil)]
    (tr d))

  (tr
    (let [faint-grey "#777777"
          defs [:defs
                 (dp/drop-shadow-effect :ds {:opacity 0.3 :offset [5 5] :radius 6})
                 ; (ds/css [[:rect {:fill :none :stroke faint-grey}]
                 ;          [:.box-text {:stroke :black}]])
                          ]]
       [:dali/page
         defs
         (element "A thing" "Marketplace" :system [10 10])
         (element "B" "BILCAS" :service [200 10])
       ])))
