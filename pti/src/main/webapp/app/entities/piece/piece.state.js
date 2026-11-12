(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('piece', {
            parent: 'entity',
            url: '/piece?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.piece.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/piece/pieces.html',
                    controller: 'PieceController',
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
                    $translatePartialLoader.addPart('piece');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('piece-detail', {
            parent: 'piece',
            url: '/piece/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.piece.detail.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/piece/piece-detail.html',
                    controller: 'PieceDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('piece');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'Piece', function($stateParams, Piece) {
                    return Piece.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'piece',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('piece-detail.edit', {
            parent: 'piece-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/piece/piece-dialog.html',
                    controller: 'PieceDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Piece', function(Piece) {
                            return Piece.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('piece.new', {
            parent: 'piece',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/piece/piece-dialog.html',
                    controller: 'PieceDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                referencePiece: null,
                                dateDelivrance: null,
                                dateExpiration: null,
                                copie: null,
                                copieContentType: null,
                                lieuDelivrance: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('piece', null, { reload: 'piece' });
                }, function() {
                    $state.go('piece');
                });
            }]
        })
        .state('piece.edit', {
            parent: 'piece',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/piece/piece-dialog.html',
                    controller: 'PieceDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Piece', function(Piece) {
                            return Piece.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('piece', null, { reload: 'piece' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('piece.delete', {
            parent: 'piece',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/piece/piece-delete-dialog.html',
                    controller: 'PieceDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['Piece', function(Piece) {
                            return Piece.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('piece', null, { reload: 'piece' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
