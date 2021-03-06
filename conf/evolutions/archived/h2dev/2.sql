-- OwcDocuments schema

# --- !Ups

-- # trait OwcFeatureType, impl are OwcEntry (which contains Offerings), and OwcDocument (which contains OwcEntries)
CREATE TABLE owc_feature_types (
  id varchar(2047) NOT NULL,
  feature_type varchar(255) NOT NULL,
  bbox varchar(255),
  PRIMARY KEY (id)
);

-- # can have owc_feature_type (if feature_type is 'OwcDocument'): foreign key owc_feature_type -> owc_feature_type n:n,
CREATE TABLE owc_feature_types_as_document_has_owc_entries (
  owc_feature_types_as_document_id varchar(2047) REFERENCES owc_feature_types(id),
  owc_feature_types_as_entry_id varchar(2047) REFERENCES owc_feature_types(id)
);

CREATE TABLE owc_authors (
  uuid varchar(255) NOT NULL,
  name varchar(255) NOT NULL,
  email varchar(255),
  uri varchar(2047),
  PRIMARY KEY (uuid)
);
-- # belongs to owc_properties_has_owc_authors: owc_author -> owc_properties (authors) n:n,
-- # belongs to owc_properties_has_owc_authors_as_contributors: owc_author -> owc_properties (contributors) n:n,

CREATE TABLE owc_categories (
  uuid varchar(255) NOT NULL,
  scheme varchar(255) NOT NULL,
  term varchar(255) NOT NULL,
  label varchar(255),
  PRIMARY KEY (uuid)
);

-- # belongs to owc_properties_has_owc_category: foreign key owc_category -> owc_properties (categories)  n:n,

CREATE TABLE owc_links (
  uuid varchar(255) NOT NULL,
  rel varchar(255) NOT NULL,
  mime_type varchar(255),
  href varchar(2047) NOT NULL,
  title varchar(255),
  PRIMARY KEY (uuid)
);

-- # belongs to owc_properties_has_owc_links: foreign key owc_links -> owc_properties (links) n:n,

CREATE TABLE owc_properties (
  uuid varchar(255) NOT NULL,
  language varchar(255) NOT NULL,
  title varchar(255) NOT NULL,
  subtitle varchar(255),
  updated TIMESTAMP,
  generator varchar(255),
  rights varchar(255),
  creator varchar(255),
  publisher varchar(255),
  PRIMARY KEY (uuid)
);
-- # belongs to owc_feature_type_has_owc_properties: foreign key owc_properties -> owc_feature_type n:n,
-- # has properties: foreign key owc_feature_type -> owc_properties n:n,
CREATE TABLE owc_feature_types_has_owc_properties (
  owc_feature_types_id varchar(2047) REFERENCES owc_feature_types(id),
  owc_properties_uuid varchar(255) REFERENCES owc_properties(uuid)
);

-- # has authors: foreign key owc_properties -> owc_author n:n,
CREATE TABLE owc_properties_has_owc_authors (
  owc_properties_uuid varchar(255) REFERENCES owc_properties(uuid),
  owc_authors_uuid varchar(255) REFERENCES owc_authors(uuid)
);

-- # has contributors: foreign key owc_properties -> owc_author n:n,
CREATE TABLE owc_properties_has_owc_authors_as_contributors (
  owc_properties_uuid varchar(255) REFERENCES owc_properties(uuid),
  owc_authors_as_contributors_uuid varchar(255) REFERENCES owc_authors(uuid)
);

-- # has categories: foreign key owc_properties -> owc_category n:n,
CREATE TABLE owc_properties_has_owc_categories (
  owc_properties_uuid varchar(255) REFERENCES owc_properties(uuid),
  owc_categories_uuid VARCHAR(255) REFERENCES owc_categories(uuid)
);

-- # has links: foreign key owc_properties -> owc_links n:n,
CREATE TABLE owc_properties_has_owc_links (
  owc_properties_uuid varchar(255) REFERENCES owc_properties(uuid),
  owc_links_uuid VARCHAR(255) REFERENCES owc_links(uuid)
);

-- # trait OwcOffering, impl are WfsOffering, WmsOffering, etc .. type is based on 'code' property
CREATE TABLE owc_offerings (
  uuid varchar(255),
  offering_type varchar(255) NOT NULL,
  code varchar(255) NOT NULL,
  content text,
  PRIMARY KEY (uuid)
);
-- # belongs to owc_feature_type_as_entry_has_owc_offerings: foreign key owc_offering -> owc_feature_type n:n,
-- # can have owc_offerings (if feature_type is 'OwcEntry'): foreign key owc_feature_type -> owc_offering n:n,
CREATE TABLE owc_feature_types_as_entry_has_owc_offerings (
  owc_feature_types_as_entry_id varchar(2047) REFERENCES owc_feature_types(id),
  owc_offerings_uuid varchar(255) REFERENCES owc_offerings(uuid)
);

CREATE TABLE owc_operations (
  uuid varchar(255),
  code varchar(255) NOT NULL,
  method varchar(255) NOT NULL,
  content_type varchar(255) NOT NULL,
  href varchar(2047) NOT NULL,
  request_content_type varchar(255),
  request_post_data text,
  result_content_type varchar(255),
  result_data text,
  PRIMARY KEY (uuid)
);
-- # belongs to owc_offering_has_owc_operation: foreign key owc_operation -> owc_offering n:n,
-- # has owc_operation: foreign key owc_offering -> owc_operation n:n,
CREATE TABLE owc_offerings_has_owc_operations (
  owc_offerings_uuid varchar(255) REFERENCES owc_offerings(uuid),
  owc_operations_uuid varchar(255) REFERENCES owc_operations(uuid)
);

# --- !Downs

DROP TABLE owc_feature_types_as_document_has_owc_entries;
DROP TABLE owc_feature_types_as_entry_has_owc_offerings;
DROP TABLE owc_feature_types_has_owc_properties;
DROP TABLE owc_properties_has_owc_authors;
DROP TABLE owc_properties_has_owc_authors_as_contributors;
DROP TABLE owc_properties_has_owc_categories;
DROP TABLE owc_properties_has_owc_links;
DROP TABLE owc_offerings_has_owc_operations;

DROP TABLE owc_operations;
DROP TABLE owc_offerings;
DROP TABLE owc_properties;
DROP TABLE owc_links;
DROP TABLE owc_categories;
DROP TABLE owc_authors;
DROP TABLE owc_feature_types;
