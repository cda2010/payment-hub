(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('parametre-methode', {
            parent: 'entity',
            url: '/parametre-methode/methode/{idmethode}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ParametreMethodes'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/parametre-methode/parametre-methodes.html',
                    controller: 'ParametreMethodeController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
            }
        })
        .state('parametre-methode-detail', {
            parent: 'parametre-methode',
            url: '/parametre-methode/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ParametreMethode'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/parametre-methode/parametre-methode-detail.html',
                    controller: 'ParametreMethodeDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                entity: ['$stateParams', 'ParametreMethode', function($stateParams, ParametreMethode) {
                    return ParametreMethode.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'parametre-methode',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('parametre-methode-detail.edit', {
            parent: 'parametre-methode-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/parametre-methode/parametre-methode-dialog.html',
                    controller: 'ParametreMethodeDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['ParametreMethode', function(ParametreMethode) {
                            return ParametreMethode.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('parametre-methode.new', {
            parent: 'parametre-methode',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/parametre-methode/parametre-methode-dialog.html',
                    controller: 'ParametreMethodeDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                nomParametreUtil: null,
                                valeur: null,
                                id: null,
                                methode:{id:$stateParams.idmethode}
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('parametre-methode', null, { reload: 'parametre-methode' });
                }, function() {
                    $state.go('parametre-methode');
                });
            }]
        })
        .state('parametre-methode.edit', {
            parent: 'parametre-methode',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/parametre-methode/parametre-methode-dialog.html',
                    controller: 'ParametreMethodeDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['ParametreMethode', function(ParametreMethode) {
                            return ParametreMethode.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('parametre-methode', null, { reload: 'parametre-methode' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('parametre-methode.delete', {
            parent: 'parametre-methode',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/parametre-methode/parametre-methode-delete-dialog.html',
                    controller: 'ParametreMethodeDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['ParametreMethode', function(ParametreMethode) {
                            return ParametreMethode.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('parametre-methode', null, { reload: 'parametre-methode' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
