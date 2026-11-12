(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('canal-communication', {
            parent: 'entity',
            url: '/canal-communication',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.canalCommunication.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/canal-communication/canal-communications.html',
                    controller: 'CanalCommunicationController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('canalCommunication');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('canal-communication-detail', {
            parent: 'canal-communication',
            url: '/canal-communication/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.canalCommunication.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/canal-communication/canal-communication-detail.html',
                    controller: 'CanalCommunicationDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('canalCommunication');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'CanalCommunication', function($stateParams, CanalCommunication) {
                    return CanalCommunication.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'canal-communication',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('canal-communication-detail.edit', {
            parent: 'canal-communication-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/canal-communication/canal-communication-dialog.html',
                    controller: 'CanalCommunicationDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['CanalCommunication', function(CanalCommunication) {
                            return CanalCommunication.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('canal-communication.new', {
            parent: 'canal-communication',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/canal-communication/canal-communication-dialog.html',
                    controller: 'CanalCommunicationDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('canal-communication', null, { reload: 'canal-communication' });
                }, function() {
                    $state.go('canal-communication');
                });
            }]
        })
        .state('canal-communication.edit', {
            parent: 'canal-communication',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/canal-communication/canal-communication-dialog.html',
                    controller: 'CanalCommunicationDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['CanalCommunication', function(CanalCommunication) {
                            return CanalCommunication.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('canal-communication', null, { reload: 'canal-communication' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('canal-communication.delete', {
            parent: 'canal-communication',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/canal-communication/canal-communication-delete-dialog.html',
                    controller: 'CanalCommunicationDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['CanalCommunication', function(CanalCommunication) {
                            return CanalCommunication.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('canal-communication', null, { reload: 'canal-communication' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
