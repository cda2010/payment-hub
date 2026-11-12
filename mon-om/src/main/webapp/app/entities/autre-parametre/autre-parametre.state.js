(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('autre-parametre', {
            parent: 'entity',
            url: '/autre-parametre',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'AutreParametres'
            },
            views: {
                'content@app': {
                      templateUrl: 'app/entities/autre-parametre/autre-parametres.html',

                    controller: 'AutreParametreController',
                    controllerAs: 'vm'
                }
            },
            params: {
                page: {
                    value: '1',
                    squash: true
                },
                sort: {
                    value: 'codeParametre,asc',
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
            }
        })
        .state('autre-parametre-detail', {
            parent: 'autre-parametre',
            url: '/autre-parametre/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'AutreParametre'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/autre-parametre/autre-parametre-detail.html',
                    controller: 'AutreParametreDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                entity: ['$stateParams', 'AutreParametre', function($stateParams, AutreParametre) {
                    return AutreParametre.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'autre-parametre',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('autre-parametre-detail.edit', {
            parent: 'autre-parametre-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/autre-parametre/autre-parametre-dialog.html',
                    controller: 'AutreParametreDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['AutreParametre', function(AutreParametre) {
                            return AutreParametre.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('autre-parametre.new', {
            parent: 'autre-parametre',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/autre-parametre/autre-parametre-dialog.html',
                    controller: 'AutreParametreDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                codeParametre: null,
                                nomParametre: null,
                                valeur: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('autre-parametre', null, { reload: 'autre-parametre' });
                }, function() {
                    $state.go('autre-parametre');
                });
            }]
        })
        .state('autre-parametre.edit', {
            parent: 'autre-parametre',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/autre-parametre/autre-parametre-dialog.html',
                    controller: 'AutreParametreDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['AutreParametre', function(AutreParametre) {
                            return AutreParametre.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('autre-parametre', null, { reload: 'autre-parametre' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('autre-parametre.delete', {
            parent: 'autre-parametre',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/autre-parametre/autre-parametre-delete-dialog.html',
                    controller: 'AutreParametreDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['AutreParametre', function(AutreParametre) {
                            return AutreParametre.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('autre-parametre', null, { reload: 'autre-parametre' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
