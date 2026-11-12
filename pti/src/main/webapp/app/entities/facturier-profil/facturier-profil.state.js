(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('facturier-profil', {
            parent: 'entity',
            url: '/facturierProfil?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.facturierProfil.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/facturier-profil/facturier-profil.html',
                    controller: 'FacturierProfilController', 
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
        .state('facturier-profil.new', {
            parent: 'facturier-profil',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/facturier-profil/facturier-profil-dialog.html',
                    controller: 'FacturierProfilDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: function () {
                            return {
                                typeActeur: null,
                                id: null,
                                facturier:null,
                                groupeProfilActeur:null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('facturier-profil', null, { reload: 'facturier-profil' });
                }, function() {
                    $state.go('facturier-profil');
                });
            }]
        })
        .state('facturier-profil.edit', {
            parent: 'facturier-profil',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/facturier-profil/facturier-profil-dialog.html',
                    controller: 'FacturierProfilDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['FacturierProfil', function(FacturierProfil) {
                            return FacturierProfil.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('facturier-profil', null, { reload: 'facturier-profil' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('facturier-profil.delete', {
            parent: 'facturier-profil',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/facturier-profil/facturier-profil-delete-dialog.html',
                    controller: 'FacturierProfilDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['FacturierProfil', function(FacturierProfil) {
                            return FacturierProfil.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('facturier-profil', null, { reload: 'facturier-profil' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
