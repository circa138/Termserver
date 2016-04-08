/* 
 * CTS2 based Terminology Server and Terminology Browser
 * Copyright (C) 2014 FH Dortmund: Peter Haas, Robert Muetzner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fhdo.terminologie.db.hibernate;
// Generated 24.10.2011 10:08:21 by Hibernate Tools 3.2.1.GA

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * CodeSystemVersion generated by hbm2java
 *
 * 16.04.2013: validityRange hinzugefügt
 */
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlType(namespace = "de.fhdo.termserver.types")
@Entity
@Table(name = "code_system_version"
)
public class CodeSystemVersion implements java.io.Serializable
{
  private Long versionId;
  private CodeSystem codeSystem;
  private Long previousVersionId;
  private String name;
  private Integer status;
  private Date statusDate;
  private Date releaseDate;
  private Date expirationDate;
  private String source;
  private String description;
  private String preferredLanguageCd;
  private String oid;
  private String licenceHolder;
  private String availableLanguages;
  private Boolean underLicence;
  private Date insertTimestamp;
  private Long validityRange;
  private Date lastChangeDate;
  private Set<CodeSystemVersionEntityMembership> codeSystemVersionEntityMemberships = new HashSet<CodeSystemVersionEntityMembership>(0);
  private Set<LicencedUser> licencedUsers = new HashSet<LicencedUser>(0);
  private Set<LicenceType> licenceTypes = new HashSet<LicenceType>(0);

  public CodeSystemVersion()
  {
  }

  public CodeSystemVersion(CodeSystem codeSystem, String name, Date statusDate, Date insertTimestamp)
  {
    this.codeSystem = codeSystem;
    this.name = name;
    this.statusDate = statusDate;
    this.insertTimestamp = insertTimestamp;
  }

  public CodeSystemVersion(CodeSystem codeSystem, Long previousVersionId, String name, Integer status, Date statusDate, Date releaseDate, Date expirationDate, String source, String description, String preferredLanguageCd, String oid, String licenceHolder, Boolean underLicence, Date insertTimestamp, Long validityRange, Date lastChangeDate, Set<CodeSystemVersionEntityMembership> codeSystemVersionEntityMemberships, Set<LicencedUser> licencedUsers, Set<LicenceType> licenceTypes)
  {
    this.codeSystem = codeSystem;
    this.previousVersionId = previousVersionId;
    this.name = name;
    this.status = status;
    this.statusDate = statusDate;
    this.releaseDate = releaseDate;
    this.expirationDate = expirationDate;
    this.source = source;
    this.description = description;
    this.preferredLanguageCd = preferredLanguageCd;
    this.oid = oid;
    this.licenceHolder = licenceHolder;
    this.underLicence = underLicence;
    this.insertTimestamp = insertTimestamp;
    this.codeSystemVersionEntityMemberships = codeSystemVersionEntityMemberships;
    this.licencedUsers = licencedUsers;
    this.licenceTypes = licenceTypes;
    this.validityRange = validityRange;
    this.lastChangeDate = lastChangeDate;
  }

  @Id
  @GeneratedValue(strategy = IDENTITY)

  @Column(name = "versionId", unique = true, nullable = false)
  public Long getVersionId()
  {
    return this.versionId;
  }

  public void setVersionId(Long versionId)
  {
    this.versionId = versionId;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "codeSystemId", nullable = false)
  public CodeSystem getCodeSystem()
  {
    return this.codeSystem;
  }

  public void setCodeSystem(CodeSystem codeSystem)
  {
    this.codeSystem = codeSystem;
  }

  @Column(name = "previousVersionID")
  public Long getPreviousVersionId()
  {
    return this.previousVersionId;
  }

  public void setPreviousVersionId(Long previousVersionId)
  {
    this.previousVersionId = previousVersionId;
  }

  @Column(name = "name", nullable = false, length = 100)
  public String getName()
  {
    return this.name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  @Column(name = "status")
  public Integer getStatus()
  {
    return this.status;
  }

  public void setStatus(Integer status)
  {
    this.status = status;
  }

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "statusDate", nullable = false, length = 19)
  public Date getStatusDate()
  {
    return this.statusDate;
  }

  public void setStatusDate(Date statusDate)
  {
    this.statusDate = statusDate;
  }

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "lastChangeDate", length = 19)
  public Date getLastChangeDate()
  {
    return this.lastChangeDate;
  }

  public void setLastChangeDate(Date lastChangeDate)
  {
    this.lastChangeDate = lastChangeDate;
  }

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "releaseDate", length = 19)
  public Date getReleaseDate()
  {
    return this.releaseDate;
  }

  public void setReleaseDate(Date releaseDate)
  {
    this.releaseDate = releaseDate;
  }

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "expirationDate", length = 19)
  public Date getExpirationDate()
  {
    return this.expirationDate;
  }

  public void setExpirationDate(Date expirationDate)
  {
    this.expirationDate = expirationDate;
  }

  @Column(name = "source", length = 65535)
  public String getSource()
  {
    return this.source;
  }

  public void setSource(String source)
  {
    this.source = source;
  }

  @Column(name = "description", length = 65535)
  public String getDescription()
  {
    return this.description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  @Column(name = "preferredLanguageCd")
  public String getPreferredLanguageCd()
  {
    return this.preferredLanguageCd;
  }

  public void setPreferredLanguageCd(String preferredLanguageCd)
  {
    this.preferredLanguageCd = preferredLanguageCd;
  }

  @Column(name = "oid", length = 100)
  public String getOid()
  {
    return this.oid;
  }

  public void setOid(String oid)
  {
    this.oid = oid;
  }

  @Column(name = "licenceHolder", length = 65535)
  public String getLicenceHolder()
  {
    return this.licenceHolder;
  }

  public void setLicenceHolder(String licenceHolder)
  {
    this.licenceHolder = licenceHolder;
  }

  @Column(name = "underLicence")
  public Boolean getUnderLicence()
  {
    return this.underLicence;
  }

  public void setUnderLicence(Boolean underLicence)
  {
    this.underLicence = underLicence;
  }

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "insertTimestamp", nullable = false, length = 19)
  public Date getInsertTimestamp()
  {
    return this.insertTimestamp;
  }

  public void setInsertTimestamp(Date insertTimestamp)
  {
    this.insertTimestamp = insertTimestamp;
  }

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "codeSystemVersion")
  public Set<CodeSystemVersionEntityMembership> getCodeSystemVersionEntityMemberships()
  {
    return this.codeSystemVersionEntityMemberships;
  }

  public void setCodeSystemVersionEntityMemberships(Set<CodeSystemVersionEntityMembership> codeSystemVersionEntityMemberships)
  {
    this.codeSystemVersionEntityMemberships = codeSystemVersionEntityMemberships;
  }

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "codeSystemVersion")
  public Set<LicencedUser> getLicencedUsers()
  {
    return this.licencedUsers;
  }

  public void setLicencedUsers(Set<LicencedUser> licencedUsers)
  {
    this.licencedUsers = licencedUsers;
  }

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "codeSystemVersion")
  public Set<LicenceType> getLicenceTypes()
  {
    return this.licenceTypes;
  }

  public void setLicenceTypes(Set<LicenceType> licenceTypes)
  {
    this.licenceTypes = licenceTypes;
  }

  @Column(name = "validityRange")
  public Long getValidityRange()
  {
    return this.validityRange;
  }

  public void setValidityRange(Long validityRange)
  {
    this.validityRange = validityRange;
  }

  /**
   * @return the availableLanguages
   */
  @Column(name = "availableLanguages")
  public String getAvailableLanguages()
  {
    return availableLanguages;
  }

  /**
   * @param availableLanguages the availableLanguages to set
   */
  public void setAvailableLanguages(String availableLanguages)
  {
    this.availableLanguages = availableLanguages;
  }
}
