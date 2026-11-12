(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('service-profil', {
            parent: 'entity',
            url: '/service-profil?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.serviceProfil.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/components/tables/tables.html',
                    controller: 'ServiceProfilController',
                    controllerAs: 'vm'
                }
            },
            params: {
                page: {
                    value: '1',
                    squash: true
                },
                sort: {
                    value: 'id,asc',
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
                    $translatePartialLoader.addPart('serviceProfil');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('service-profil-detail', {
            parent: 'service-profil',
            url: '/service-profil/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.serviceProfil.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/service-profil/service-profil-detail.html',
                    controller: 'ServiceProfilDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('serviceProfil');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'ServiceProfil', function($stateParams, ServiceProfil) {
                    return ServiceProfil.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'service-profil',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('service-profil-detail.edit', {
            parent: 'service-profil-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/service-profil/service-profil-dialog.html',
                    controller: 'ServiceProfilDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['ServiceProfil', function(ServiceProfil) {
                            return ServiceProfil.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('service-profil.new', {
            parent: 'service-profil',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/service-profil/service-profil-dialog.html',
                    controller: 'ServiceProfilDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: function () {
                            return {
                                nom: null,
                                description: null,
                                id: null,
                                typeProfilService:null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('service-profil', null, { reload: 'service-profil' });
                }, function() {
                    $state.go('service-profil');
                });
            }]
        })
        .state('service-profil.edit', {
            parent: 'service-profil',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/service-profil/service-profil-dialog.html',
                    controller: 'ServiceProfilDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['ServiceProfil', function(ServiceProfil) {
                            return ServiceProfil.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('service-profil', null, { reload: 'service-profil' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('service-profil.delete', {
            parent: 'service-profil',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/service-profil/service-profil-delete-dialog.html',
                    controller: 'ServiceProfilDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['ServiceProfil', function(ServiceProfil) {
                            return ServiceProfil.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('service-profil', null, { reload: 'service-profil' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
