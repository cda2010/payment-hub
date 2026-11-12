(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('beneficiaire', {
            parent: 'entity',
            url: '/beneficiaire?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.beneficiaire.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/beneficiaire/beneficiaires.html',
                    controller: 'BeneficiaireController',
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
                    $translatePartialLoader.addPart('beneficiaire');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('beneficiaire-detail', {
            parent: 'beneficiaire',
            url: '/beneficiaire/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.beneficiaire.detail.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/beneficiaire/beneficiaire-detail.html',
                    controller: 'BeneficiaireDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('beneficiaire');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'Beneficiaire', function($stateParams, Beneficiaire) {
                    return Beneficiaire.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'beneficiaire',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('beneficiaire-detail.edit', {
            parent: 'beneficiaire-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/beneficiaire/beneficiaire-dialog.html',
                    controller: 'BeneficiaireDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Beneficiaire', function(Beneficiaire) {
                            return Beneficiaire.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('beneficiaire.new', {
            parent: 'beneficiaire',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/beneficiaire/beneficiaire-dialog.html',
                    controller: 'BeneficiaireDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                code: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('beneficiaire', null, { reload: 'beneficiaire' });
                }, function() {
                    $state.go('beneficiaire');
                });
            }]
        })
        .state('beneficiaire.edit', {
            parent: 'beneficiaire',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/beneficiaire/beneficiaire-dialog.html',
                    controller: 'BeneficiaireDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Beneficiaire', function(Beneficiaire) {
                            return Beneficiaire.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('beneficiaire', null, { reload: 'beneficiaire' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('beneficiaire.delete', {
            parent: 'beneficiaire',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/beneficiaire/beneficiaire-delete-dialog.html',
                    controller: 'BeneficiaireDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['Beneficiaire', function(Beneficiaire) {
                            return Beneficiaire.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('beneficiaire', null, { reload: 'beneficiaire' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
