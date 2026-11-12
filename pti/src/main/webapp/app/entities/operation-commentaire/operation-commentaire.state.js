(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('operation-commentaire', {
            parent: 'entity',
            url: '/operation-commentaire?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.operationCommentaire.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/operation-commentaire/operation-commentaires.html',
                    controller: 'OperationCommentaireController',
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
                    $translatePartialLoader.addPart('operationCommentaire');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('operation-commentaire-detail', {
            parent: 'operation-commentaire',
            url: '/operation-commentaire/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.operationCommentaire.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/operation-commentaire/operation-commentaire-detail.html',
                    controller: 'OperationCommentaireDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('operationCommentaire');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'OperationCommentaire', function($stateParams, OperationCommentaire) {
                    return OperationCommentaire.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'operation-commentaire',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('operation-commentaire-detail.edit', {
            parent: 'operation-commentaire-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/operation-commentaire/operation-commentaire-dialog.html',
                    controller: 'OperationCommentaireDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['OperationCommentaire', function(OperationCommentaire) {
                            return OperationCommentaire.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('operation-commentaire.new', {
            parent: 'operation-commentaire',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/operation-commentaire/operation-commentaire-dialog.html',
                    controller: 'OperationCommentaireDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                commentaire: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('operation-commentaire', null, { reload: 'operation-commentaire' });
                }, function() {
                    $state.go('operation-commentaire');
                });
            }]
        })
        .state('operation-commentaire.edit', {
            parent: 'operation-commentaire',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/operation-commentaire/operation-commentaire-dialog.html',
                    controller: 'OperationCommentaireDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['OperationCommentaire', function(OperationCommentaire) {
                            return OperationCommentaire.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('operation-commentaire', null, { reload: 'operation-commentaire' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('operation-commentaire.delete', {
            parent: 'operation-commentaire',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/operation-commentaire/operation-commentaire-delete-dialog.html',
                    controller: 'OperationCommentaireDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['OperationCommentaire', function(OperationCommentaire) {
                            return OperationCommentaire.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('operation-commentaire', null, { reload: 'operation-commentaire' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
