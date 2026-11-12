(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('agence', {
            parent: 'entity',
            url: '/agence/{idFournisseur}?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'Agences'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/agence/agences.html',
                    controller: 'AgenceController',
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
                fournisseur: ['$stateParams', 'FournisseurService', function($stateParams, FournisseurService) {
                         return FournisseurService.get({id : $stateParams.idFournisseur}).$promise;
                    
                }],
                pagingParams: ['$stateParams', 'PaginationUtil', function ($stateParams, PaginationUtil) {
                    return {
                        fseId:$stateParams.idFournisseur,
                        page: PaginationUtil.parsePage($stateParams.page),
                        sort: $stateParams.sort,
                        predicate: PaginationUtil.parsePredicate($stateParams.sort),
                        ascending: PaginationUtil.parseAscending($stateParams.sort),
                        search: $stateParams.search
                    };
                }],
            }
        })
        .state('agence-detail', {
            parent: 'agence',
            url: '/agence/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'Agence'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/agence/agence-detail.html',
                    controller: 'AgenceDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                entity: ['$stateParams', 'Agence', function($stateParams, Agence) {
                    return Agence.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'agence',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('agence-detail.edit', {
            parent: 'agence-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/agence/agence-dialog.html',
                    controller: 'AgenceDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Agence', function(Agence) {
                            return Agence.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('agence.new', {
            parent: 'agence',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                  
                $uibModal.open({
                    templateUrl: 'app/entities/agence/agence-dialog.html',
                    controller: 'AgenceDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                id: null,
                                fournisseurService:{id:$stateParams.idFournisseur}
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('agence', null, { reload: 'agence' });
                }, function() {
                    $state.go('agence');
                });
            }]
        })
        .state('agence.edit', {
            parent: 'agence',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/agence/agence-dialog.html',
                    controller: 'AgenceDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Agence', function(Agence) {
                            return Agence.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('agence', null, { reload: 'agence' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('agence.delete', {
            parent: 'agence',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/agence/agence-delete-dialog.html',
                    controller: 'AgenceDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['Agence', function(Agence) {
                            return Agence.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('agence', null, { reload: 'agence' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
