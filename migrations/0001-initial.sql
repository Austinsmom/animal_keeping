-- MySQL Script generated by MySQL Workbench
-- Sun Feb  5 18:14:53 2017
-- Model: New Model    Version: 1.0
-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';



-- -----------------------------------------------------
-- Table `census_person`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `census_person` (
  `id` INT NOT NULL,
  `first` VARCHAR(200) NOT NULL,
  `last` VARCHAR(200) NOT NULL,
  `email` VARCHAR(200) NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `census_license`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `census_license` (
  `id` INT NOT NULL,
  `name` VARCHAR(200) NOT NULL,
  `number` VARCHAR(200) NOT NULL,
  `start_date` DATE NULL,
  `end_date` DATE NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `census_speciestype`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `census_speciestype` (
  `id` INT NOT NULL,
  `name` VARCHAR(200) NOT NULL,
  `trivial` VARCHAR(200) NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `census_quota`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `census_quota` (
  `id` INT NOT NULL,
  `species_id` INT NOT NULL,
  `license_id` INT NOT NULL,
  `number` INT NULL,
  PRIMARY KEY (`id`),
  INDEX `species_id_idx` (`species_id` ASC),
  INDEX `license_id_idx` (`license_id` ASC),
  CONSTRAINT `q_species_id`
    FOREIGN KEY (`species_id`)
    REFERENCES `census_speciestype` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `q_license_id`
    FOREIGN KEY (`license_id`)
    REFERENCES `census_license` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `census_suppliertype`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `census_suppliertype` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(200) NOT NULL,
  `address` LONGTEXT NOT NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `census_subjecttype`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `census_subjecttype` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(500) NOT NULL,
  `description` LONGTEXT NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `census_subject`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `census_subject` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(200) NOT NULL,
  `type_id` INT NOT NULL,
  `species_id` INT NOT NULL,
  `alias` VARCHAR(200) NULL,
  `source_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `species_id_idx` (`species_id` ASC),
  INDEX `source_id_idx` (`source_id` ASC),
  INDEX `type_id_idx` (`type_id` ASC),
  CONSTRAINT `sb_species_id`
    FOREIGN KEY (`species_id`)
    REFERENCES `census_speciestype` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `sb_source_id`
    FOREIGN KEY (`source_id`)
    REFERENCES `census_suppliertype` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `sb_type_id`
    FOREIGN KEY (`type_id`)
    REFERENCES `census_subjecttype` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `census_subjectnote`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `census_subjectnote` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(200) NOT NULL,
  `content` LONGTEXT NOT NULL,
  `date` DATE NOT NULL,
  `person_id` INT NULL,
  `subject_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `subject_id_idx` (`subject_id` ASC),
  INDEX `person_id_idx` (`person_id` ASC),
  CONSTRAINT `sn_subject_id`
    FOREIGN KEY (`subject_id`)
    REFERENCES `census_subject` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `sn_person_id`
    FOREIGN KEY (`person_id`)
    REFERENCES `census_person` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `census_treatmenttype`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `census_treatmenttype` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(200) NOT NULL,
  `description` LONGTEXT NULL,
  `license_id` INT NULL,
  `invasive` TINYINT(1) NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `tt_license_id_idx` (`license_id` ASC),
  CONSTRAINT `tt_license_id`
    FOREIGN KEY (`license_id`)
    REFERENCES `census_license` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `census_treatment`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `census_treatment` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(200) NOT NULL,
  `type_id` INT NOT NULL,
  `subject_id` INT NOT NULL,
  `person_id` INT NOT NULL,
  `start_datetime` DATETIME NOT NULL,
  `end_datetime` DATETIME NULL,
  PRIMARY KEY (`id`),
  INDEX `subject_id_idx` (`subject_id` ASC),
  INDEX `person_id_idx` (`person_id` ASC),
  INDEX `type_id_idx` (`type_id` ASC),
  CONSTRAINT `t_subject_id`
    FOREIGN KEY (`subject_id`)
    REFERENCES `census_subject` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `t_person_id`
    FOREIGN KEY (`person_id`)
    REFERENCES `census_person` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `t_type_id`
    FOREIGN KEY (`type_id`)
    REFERENCES `census_treatmenttype` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `census_treatmentnote`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `census_treatmentnote` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(200) NOT NULL,
  `content` LONGTEXT NULL,
  `person_id` INT NULL,
  `treatment_id` INT NOT NULL,
  `date` DATE NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `person_id_idx` (`person_id` ASC),
  INDEX `treatment_id_idx` (`treatment_id` ASC),
  CONSTRAINT `tn_person_id`
    FOREIGN KEY (`person_id`)
    REFERENCES `census_person` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `tn_treatment_id`
    FOREIGN KEY (`treatment_id`)
    REFERENCES `census_treatment` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `census_housingtype`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `census_housingtype` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(200) NOT NULL,
  `description` LONGTEXT NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `census_housingunit`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `census_housingunit` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(200) NOT NULL,
  `type_id` INT NOT NULL,
  `parent_unit_id` INT NULL,
  `description` LONGTEXT NULL,
  `dimensions` VARCHAR(200) NULL,
  PRIMARY KEY (`id`),
  INDEX `parent_unit_id_idx` (`parent_unit_id` ASC),
  INDEX `type_id_idx` (`type_id` ASC),
  CONSTRAINT `hu_parent_unit_id`
    FOREIGN KEY (`parent_unit_id`)
    REFERENCES `census_housingunit` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `hu_type_id`
    FOREIGN KEY (`type_id`)
    REFERENCES `census_housingtype` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `census_housing`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `census_housing` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `start_datetime` DATETIME NOT NULL,
  `end_datetime` DATETIME NULL,
  `subject_id` INT NOT NULL,
  `type_id` INT NOT NULL,
  `comment` LONGTEXT NULL,
  PRIMARY KEY (`id`),
  INDEX `subject_id_idx` (`subject_id` ASC),
  CONSTRAINT `h_subject_id`
    FOREIGN KEY (`subject_id`)
    REFERENCES `census_subject` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `h_type_id`
    FOREIGN KEY (`type_id`)
    REFERENCES `census_housingunit` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `census_housingnote`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `census_housingnote` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `person_id` INT NULL,
  `housing_id` INT NOT NULL,
  `name` VARCHAR(200) NOT NULL,
  `content` LONGTEXT NOT NULL,
  `date` DATE NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `person_id_idx` (`person_id` ASC),
  INDEX `housing_id_idx` (`housing_id` ASC),
  CONSTRAINT `hn_housing_id`
    FOREIGN KEY (`housing_id`)
    REFERENCES `census_housing` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `hn_person_id`
    FOREIGN KEY (`person_id`)
    REFERENCES `census_person` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
