(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('alarme', {
            parent: 'entity',
            url: '/alarme',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.alarme.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/alarme/alarmes.html',
                    controller: 'AlarmeController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('alarme');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('alarme-detail', {
            parent: 'alarme',
            url: '/alarme/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.alarme.detail.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/alarme/alarme-detail.html',
                    controller: 'AlarmeDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('alarme');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'Alarme', function($stateParams, Alarme) {
                    return Alarme.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'alarme',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('alarme-detail.edit', {
            parent: 'alarme-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/alarme/alarme-dialog.html',
                    controller: 'AlarmeDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: ['Alarme', function(Alarme) {
                            return Alarme.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('alarme.new', {
            parent: 'alarme',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/alarme/alarme-dialog.html',
                    controller: 'AlarmeDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: function () {
                            return {
                                etiquette: null,
                                emails: null,
                                sms: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('alarme', null, { reload: 'alarme' });
                }, function() {
                    $state.go('alarme');
                });
            }]
        })
        .state('alarme.edit', {
            parent: 'alarme',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/alarme/alarme-dialog.html',
                    controller: 'AlarmeDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: ['Alarme', function(Alarme) {
                            return Alarme.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('alarme', null, { reload: 'alarme' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('alarme.delete', {
            parent: 'alarme',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/alarme/alarme-delete-dialog.html',
                    controller: 'AlarmeDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['Alarme', function(Alarme) {
                            return Alarme.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('alarme', null, { reload: 'alarme' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
