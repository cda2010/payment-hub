(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('compte-autre', {
            parent: 'entity',
            url: '/compte-autre?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.compteAutre.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/compte-autre/compte-autres.html',
                    controller: 'CompteAutreController',
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
                    $translatePartialLoader.addPart('compteAutre');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('compte-autre-detail', {
            parent: 'compte-autre',
            url: '/compte-autre/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.compteAutre.detail.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/compte-autre/compte-autre-detail.html',
                    controller: 'CompteAutreDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('compteAutre');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'CompteAutre', function($stateParams, CompteAutre) {
                    return CompteAutre.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'compte-autre',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('compte-autre-detail.edit', {
            parent: 'compte-autre-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/compte-autre/compte-autre-dialog.html',
                    controller: 'CompteAutreDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['CompteAutre', function(CompteAutre) {
                            return CompteAutre.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('compte-autre.new', {
            parent: 'compte-autre',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/compte-autre/compte-autre-dialog.html',
                    controller: 'CompteAutreDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                solde: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('compte-autre', null, { reload: 'compte-autre' });
                }, function() {
                    $state.go('compte-autre');
                });
            }]
        })
        .state('compte-autre.edit', {
            parent: 'compte-autre',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/compte-autre/compte-autre-dialog.html',
                    controller: 'CompteAutreDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['CompteAutre', function(CompteAutre) {
                            return CompteAutre.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('compte-autre', null, { reload: 'compte-autre' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('compte-autre.delete', {
            parent: 'compte-autre',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/compte-autre/compte-autre-delete-dialog.html',
                    controller: 'CompteAutreDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['CompteAutre', function(CompteAutre) {
                            return CompteAutre.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('compte-autre', null, { reload: 'compte-autre' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
