(ns datomic-praxis.core
  (:require [datomic.api :as d])
  (:use clojure.pprint))


(def uri "datomic:mem://seattle")

;; create database
(d/create-database uri)

;; connect to database
(def conn (d/connect uri))

;; parse schema edn file
(def schema-tx (read-string (slurp "samples/seattle/seattle-schema.edn")))

;; display first statement
(first schema-tx)

;; list all attributes
(pprint (filter identity (map :db/ident schema-tx)))

;; submit schema transaction
(d/transact conn schema-tx)

;; parse seed data edn file
(def data-tx (read-string (slurp "samples/seattle/seattle-data0.edn")))


;; display first three statements in seed data transaction
(pprint (take 3 data-tx))
;; why is :community/category wrapped in a vector?

;; submit seed data transaction
(d/transact conn data-tx)


;; find all communities, return entity ids



;; get first entity id in results and make an entity map


;; display the entity map's keys


;; display the value of the entity's community name


;; navigate to neighborhood


;; touch the nbh


;; reverse navigate to communities with that neighborhood



;; for each community, display its name & its neighborhood name
;; (this is a join)



;; find all communities in the same neighborhood as a given community, using query



;; find all communities with Beacon Hill in the name



;; turn the above query into a function




;; make function take a set of regexs






;; find all community names coming before "C" in alphabetical order




;; RULES


;; find all names of all communities that are twitter feeds, using rules


;; find names of all communities in NE and SW regions, using rules
;; to avoid repeating logic


;; find names of all communities that are in any of the northern
;; regions and are social-media, using rules for OR logic


;; Find all transaction times, sort them in reverse order



