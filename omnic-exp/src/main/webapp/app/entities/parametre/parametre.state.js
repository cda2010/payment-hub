(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('parametre', {
            parent: 'entity',
            url: '/parametre',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'Parametres'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/parametre/parametres.html',
                    controller: 'ParametreController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
            }
        })
        .state('parametre-detail', {
            parent: 'parametre',
            url: '/parametre/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'Parametre'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/parametre/parametre-detail.html',
                    controller: 'ParametreDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                entity: ['$stateParams', 'Parametre', function($stateParams, Parametre) {
                    return Parametre.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'parametre',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('parametre-detail.edit', {
            parent: 'parametre-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/parametre/parametre-dialog.html',
                    controller: 'ParametreDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Parametre', function(Parametre) {
                            return Parametre.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('parametre.new', {
            parent: 'parametre',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/parametre/parametre-dialog.html',
                    controller: 'ParametreDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                nomParametre: null,
                                typeParametre: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('parametre', null, { reload: 'parametre' });
                }, function() {
                    $state.go('parametre');
                });
            }]
        })
        .state('parametre.edit', {
            parent: 'parametre',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/parametre/parametre-dialog.html',
                    controller: 'ParametreDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Parametre', function(Parametre) {
                            return Parametre.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('parametre', null, { reload: 'parametre' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('parametre.delete', {
            parent: 'parametre',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/parametre/parametre-delete-dialog.html',
                    controller: 'ParametreDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['Parametre', function(Parametre) {
                            return Parametre.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('parametre', null, { reload: 'parametre' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
