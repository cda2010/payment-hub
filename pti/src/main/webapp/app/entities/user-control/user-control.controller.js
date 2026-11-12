(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('UserControlController', UserControlController);

    UserControlController.$inject = ['$state', 'UserControls', 'TableService', '$scope'];

    function UserControlController($state, UserControls, TableService, $scope) {

        var vm = this;
        init();
        function init(){
            var addItemData = {
                templateUrl: 'app/entities/user-control/user-control-dialog.html',
                controller: 'UserControlDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    isViewing: function () {
                        return false;
                    },
                    isEdit: function () {
                        return false;
                    },
                    entity: function () {
                        return {};
                    }
                }
            };

            var editItemData = {
                templateUrl: 'app/entities/user-control/user-control-dialog.html',
                controller: 'UserControlDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    isViewing: function () {
                        return false;
                    },
                    isEdit: function () {
                        return true;
                    },
                    entity: null //valeur déterminé à l'appel de la boite
                }
            };

            var detailItemData = {
                templateUrl: 'app/entities/user-control/user-control-dialog.html',
                controller: 'UserControlDialogController',
                controllerAs: 'vm',
                size: 'md',
                resolve: {
                    isViewing: function () {
                        return true;
                    },
                    isEdit: function () {
                        return false;
                    },
                    entity: null //valeur déterminé à l'appel de la boite,
                }
            };
            var deleteItemData = {
                templateUrl: 'app/entities/user-control/user-control-delete-dialog.html',
                controller: 'UserControlDeleteController',
                controllerAs: 'vm',
                size: 'sm',
                resolve: {
                    entity: null //valeur déterminé à l'appel de la boite
                }
            };
            var dialogData = {
                addData: addItemData,
                editData: editItemData,
                detailData: detailItemData,
                deleteData: deleteItemData
            };
            var entitySearchParams = function () {
                return {};
            };
            var entityParams = function () {
                return {
                };
            };
            var datas = {
                scope: $scope,
                vm: vm,
                entity: UserControls,
                dialogData: dialogData,
                entitySearch: UserControls,
                entitySearchParams: entitySearchParams,
                entityParams: entityParams,
                loadCallBack: vm.load
            };
            TableService.init(datas);

        }
    }
})();
