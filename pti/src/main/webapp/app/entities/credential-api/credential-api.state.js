(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('credential-api', {
            parent: 'entity',
            url: '/credential-api?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.credentialApi.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/credential-api/credential-apis.html',
                    controller: 'CredentialApiController',
                    controllerAs: 'vm'
                }
            },
            params: {
                page: {
                    value: '1',
                    squash: true
                },
                sort: {
                    value: 'client_id,asc',
                    squash: true
                },
                search: null
            },
            resolve: {
                pagingParams: ['$stateParams', 'PaginationUtil', function ($stateParams, PaginationUtil) {
                    return {
                        page: PaginationUtil.parsePage($stateParams.page),
                        sort: $stateParams.sort,
                        predicate: PaginationUtil.parsePredicate($stateParams.sort),
                        ascending: PaginationUtil.parseAscending($stateParams.sort),
                        search: $stateParams.search
                    };
                }],
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('credentialApi');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('credential-api-detail', {
            parent: 'credential-api',
            url: '/credential-api/{client_id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.credentialApi.detail.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/credential-api/credential-api-detail.html',
                    controller: 'CredentialApiDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('credentialApi');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'CredentialApi', function($stateParams, CredentialApi) {
                    return CredentialApi.getOauthById({client_id : $stateParams.client_id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'credential-api',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('credential-api-detail.edit', {
            parent: 'credential-api-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/credential-api/credential-api-dialog.html',
                    controller: 'CredentialApiDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['CredentialApi', function(CredentialApi) {
                            return CredentialApi.getOauthById({client_id : $stateParams.client_id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('credential-api.new', {
            parent: 'credential-api',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/credential-api/credential-api-dialog.html',
                    controller: 'CredentialApiDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                resource_ids: null,
                                client_secret: null,
                                scope: null,
                                authorized_grant_types: null,
                                web_server_redirect_uri: null,
                                authorities: null,
                                access_token_validity: null,
                                refresh_token_validity: null,
                                additional_information: null,
                                autoapprove: null,
                                client_id: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('credential-api', null, { reload: 'credential-api' });
                }, function() {
                    $state.go('credential-api');
                });
            }]
        })
        .state('credential-api.edit', {
            parent: 'credential-api',
            url: '/{client_id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/credential-api/credential-api-dialog.html',
                    controller: 'CredentialApiDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['CredentialApi', function(CredentialApi) {
                            return CredentialApi.getOauthById({client_id : $stateParams.client_id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('credential-api', null, { reload: 'credential-api' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('credential-api.delete', {
            parent: 'credential-api',
            url: '/{client_id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/credential-api/credential-api-delete-dialog.html',
                    controller: 'CredentialApiDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['CredentialApi', function(CredentialApi) {
                            return CredentialApi.getOauthById({client_id : $stateParams.client_id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('credential-api', null, { reload: 'credential-api' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
