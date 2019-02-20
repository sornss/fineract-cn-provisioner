/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.cn.provisioner.client;

import org.apache.fineract.cn.provisioner.AbstractServiceTest;
import org.apache.fineract.cn.provisioner.api.v1.client.DuplicateIdentifierException;
import org.apache.fineract.cn.provisioner.api.v1.domain.AuthenticationResponse;
import org.apache.fineract.cn.provisioner.api.v1.domain.Client;
import org.apache.fineract.cn.provisioner.config.ProvisionerConstants;
import org.apache.fineract.cn.api.context.AutoSeshat;
import org.apache.fineract.cn.api.util.ApiConstants;
import org.apache.fineract.cn.api.util.NotFoundException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestClients extends AbstractServiceTest {

  private AutoSeshat autoSeshat;

  public TestClients() {
    super();
  }
  @Before
  public void before()
  {
    final AuthenticationResponse authentication = provisioner.authenticate(
        this.getClientId(), ApiConstants.SYSTEM_SU, ProvisionerConstants.INITIAL_PWD);
    autoSeshat = new AutoSeshat(authentication.getToken());
  }

  @After
  public void after() {
    provisioner.deleteClient(Fixture.getCompTestClient().getName());
    autoSeshat.close();
  }

  @Test
  public void shouldCreateClient() {
    final Client client = Fixture.getCompTestClient();

    provisioner.createClient(client);
    //TODO: add waiting?

    final Client newlyCreatedClient = provisioner.getClient(client.getName());

    Assert.assertEquals(client.getName(), newlyCreatedClient.getName());
    Assert.assertEquals(client.getDescription(), newlyCreatedClient.getDescription());
    Assert.assertEquals(client.getHomepage(), newlyCreatedClient.getHomepage());
    Assert.assertEquals(client.getVendor(), newlyCreatedClient.getVendor());
    Assert.assertEquals(client.getRedirectUri(), newlyCreatedClient.getRedirectUri());
  }

  @Test(expected = DuplicateIdentifierException.class)
  public void shouldFailCreateClientAlreadyExists() {
    final Client client = new Client();
    client.setName("duplicate-client");

    provisioner.createClient(client);
    provisioner.createClient(client);
  }

  @Test
  public void shouldFindClient() {
    provisioner.createClient(Fixture.getCompTestClient());
    Assert.assertNotNull(provisioner.getClient(Fixture.getCompTestClient().getName()));
  }

  @Test(expected = NotFoundException.class)
  public void shouldNotFindClientUnknown() {
    provisioner.getClient("unknown-client");
  }

  @Test
  public void shouldFetchAllClients() {
    Assert.assertFalse(provisioner.getClients().isEmpty());
  }

  @Test
  public void shouldDeleteClient() {
    final Client clientToDelete = new Client();
    clientToDelete.setName("deleteme");

    provisioner.createClient(clientToDelete);

    try {
      provisioner.getClient(clientToDelete.getName());
    } catch (final Exception ex) {
      Assert.fail();
    }

    provisioner.deleteClient(clientToDelete.getName());

    try {
      provisioner.getClient(clientToDelete.getName());
      Assert.fail();
    }
    catch (final RuntimeException ex) {
      Assert.assertTrue(ex instanceof NotFoundException);
    }
  }
}
