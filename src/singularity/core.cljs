(ns singularity.core
  (:require [reagent.core :as r]
            [singularity.view :as view]
            [singularity.db :as db]))

(defn init []
  (db/init)
  (r/render-component (view/root) (.-body js/document)))

(enable-console-print!)
(figwheel.client/start {:on-jsload init})
(init)