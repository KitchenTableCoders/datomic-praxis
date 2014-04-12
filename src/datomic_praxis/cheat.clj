(ns datomic-praxis.cheat
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

(d/q
  '[:find ?e
    :where
    [?e :community/name _]]
  (d/db conn))

;; get first entity id in results and make an entity map

(d/entity (d/db conn) 17592186045473)

;; display the entity map's keys
(pprint (keys (d/entity (d/db conn) 17592186045473)))

;; display the value of the entity's community name
(:community/name (d/entity (d/db conn) 17592186045473))

;; navigate to neighborhood
(def nbh (:community/neighborhood (d/entity (d/db conn) 17592186045473)))

;; touch the nbh
(d/touch nbh)

;; reverse navigate to communities with that neighborhood
(:community/_neighborhood nbh)


;; for each community, display its name & its neighborhood name
;; (this is a join)
(pprint (d/q
          '[:find ?cname ?nname
            :where
            [?e :community/name ?cname]
            [?e :community/neighborhood ?n]
            [?n :neighborhood/name ?nname]
            ]
          (d/db conn)))


;; find all communities in the same neighborhood as a given community, using query
(d/q '[:find ?cname
       :where
       [17592186045473 :community/neighborhood ?n]
       [?e :community/neighborhood ?n]
       [?e :community/name ?cname]]
     (d/db conn))


;; find all communities with Beacon Hill in the name
(d/q '[:find ?cname
       :where
       [_ :community/name ?cname]
       [(re-matches #".*Beacon Hill.*" ?cname)]]
     (d/db conn))


;; turn the above query into a function
(defn communities-matching [regex]
  (d/q '[:find ?cname
         :in $ ?r
         :where
         [_ :community/name ?cname]
         [(re-matches ?r ?cname)]]
       (d/db conn)
       regex))

(communities-matching #".*Hill.*")

;; make function take a set of regexs

(defn communities-matching2 [regex]
  (d/q '[:find ?cname
         :in $ [?r]
         :where
         [_ :community/name ?cname]
         [(re-matches ?r ?cname)]]
       (d/db conn)
       regex))

(communities-matching2 [ #".*Seattle.*"])



;; find all community names coming before "C" in alphabetical order
(pprint (seq (q '[:find ?n
                  :where
                  [?c :community/name ?n]
                  [(.compareTo ?n "C") ?res]
                  [(< ?res 0)]]
                (db conn))))



;; RULES


;; find all names of all communities that are twitter feeds, using rules
(let [rules '[[[twitter ?c]
               [?c :community/type :community.type/twitter]]]]
  (pprint (seq (q '[:find ?n
                    :in $ %
                    :where
                    [?c :community/name ?n]
                    (twitter ?c)]
                  (db conn)
                  rules))))

;; find names of all communities in NE and SW regions, using rules
;; to avoid repeating logic
(let [rules '[[[region ?c ?r]
               [?c :community/neighborhood ?n]
               [?n :neighborhood/district ?d]
               [?d :district/region ?re]
               [?re :db/ident ?r]]]]
  (pprint (seq (q '[:find ?n
                    :in $ %
                    :where
                    [?c :community/name ?n]
                    [region ?c :region/ne]]
                  (db conn)
                  rules)))
  (pprint (seq (q '[:find ?n
                    :in $ %
                    :where
                    [?c :community/name ?n]
                    [region ?c :region/sw]]
                  (db conn)
                  rules))))

;; find names of all communities that are in any of the northern
;; regions and are social-media, using rules for OR logic
(let [rules '[[[region ?c ?r]
               [?c :community/neighborhood ?n]
               [?n :neighborhood/district ?d]
               [?d :district/region ?re]
               [?re :db/ident ?r]]
              [[social-media ?c]
               [?c :community/type :community.type/twitter]]
              [[social-media ?c]
               [?c :community/type :community.type/facebook-page]]
              [[northern ?c]
               (region ?c :region/ne)]
              [[northern ?c]
               (region ?c :region/n)]
              [[northern ?c]
               (region ?c :region/nw)]
              [[southern ?c]
               (region ?c :region/sw)]
              [[southern ?c]
               (region ?c :region/s)]
              [[southern ?c]
               (region ?c :region/se)]]]
  (pprint (seq (q '[:find ?n
                    :in $ %
                    :where
                    [?c :community/name ?n]
                    (southern ?c)
                    (social-media ?c)]
                  (db conn)
                  rules))))

;; Find all transaction times, sort them in reverse order
(def tx-instants (reverse (sort (q '[:find ?when :where [_ :db/txInstant ?when]]
                                   (db conn)))))


