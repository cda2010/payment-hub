(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('facture', {
            parent: 'entity',
            url: '/facture?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.facture.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/facture/factures.html',
                    controller: 'FactureController',
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
                    $translatePartialLoader.addPart('facture');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('facture-detail', {
            parent: 'facture',
            url: '/facture/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.facture.detail.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/facture/facture-detail.html',
                    controller: 'FactureDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('facture');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'Facture', function($stateParams, Facture) {
                    return Facture.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'facture',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('facture-detail.edit', {
            parent: 'facture-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/facture/facture-dialog.html',
                    controller: 'FactureDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: ['Facture', function(Facture) {
                            return Facture.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('facture.new', {
            parent: 'facture',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/facture/facture-dialog.html',
                    controller: 'FactureDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: function () {
                            return {
                                referenceFacature: null,
                                montantFacture: null,
                                referencePaiement: null,
                                datePaiement: null,
                                id: null,
                                produit:null,
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('facture', null, { reload: 'facture' });
                }, function() {
                    $state.go('facture');
                });
            }]
        })
        .state('facture.edit', {
            parent: 'facture',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/facture/facture-dialog.html',
                    controller: 'FactureDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: ['Facture', function(Facture) {
                            return Facture.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('facture', null, { reload: 'facture' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('facture.delete', {
            parent: 'facture',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/facture/facture-delete-dialog.html',
                    controller: 'FactureDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['Facture', function(Facture) {
                            return Facture.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('facture', null, { reload: 'facture' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
