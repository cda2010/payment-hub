(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('ligne-reglement', {
            parent: 'entity',
            url: '/ligne-reglement?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.ligneReglement.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/ligne-reglement/ligne-reglement-organisation.html',
                    controller: 'LigneReglementController',
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
                    $translatePartialLoader.addPart('ligneReglement');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('ligne-reglement-detail', {
            parent: 'ligne-reglement',
            url: '/ligne-reglement/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.ligneReglement.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/ligne-reglement/ligne-reglement-organisation-detail.html',
                    controller: 'LigneReglementDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('ligneReglement');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'LigneReglement', function($stateParams, LigneReglement) {
                    return LigneReglement.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'ligne-reglement',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('ligne-reglement-detail.edit', {
            parent: 'ligne-reglement-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/ligne-reglement/ligne-reglement-organisation-dialog.html',
                    controller: 'LigneReglementDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['LigneReglement', function(LigneReglement) {
                            return LigneReglement.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('ligne-reglement.new', {
            parent: 'ligne-reglement',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/ligne-reglement/ligne-reglement-organisation-dialog.html',
                    controller: 'LigneReglementDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                montantCommission: null,
                                soldeCompteOp: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('ligne-reglement', null, { reload: 'ligne-reglement' });
                }, function() {
                    $state.go('ligne-reglement');
                });
            }]
        })
        .state('ligne-reglement.edit', {
            parent: 'ligne-reglement',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/ligne-reglement/ligne-reglement-organisation-dialog.html',
                    controller: 'LigneReglementDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['LigneReglement', function(LigneReglement) {
                            return LigneReglement.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('ligne-reglement', null, { reload: 'ligne-reglement' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('ligne-reglement.delete', {
            parent: 'ligne-reglement',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/ligne-reglement/ligne-reglement-organisation-delete-dialog.html',
                    controller: 'LigneReglementDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['LigneReglement', function(LigneReglement) {
                            return LigneReglement.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('ligne-reglement', null, { reload: 'ligne-reglement' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
