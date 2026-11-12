(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TemplateMessageController', TemplateMessageController);

    TemplateMessageController.$inject = ['$state','$scope', 'TemplateMessage', 'ParseLinks', 'AlertService', 'TypeService','paginationConstants', 'pagingParams','TableService'];

    function TemplateMessageController($state,$scope, TemplateMessage, ParseLinks, AlertService, TypeService,paginationConstants, pagingParams,TableService) {

        var vm = this;
        vm.filtre=filtre;
        vm.initFiltre=initFiltre;
        ini();
        vm.typeServices = TypeService.queryAll();
        vm.itemSearch ={
            "typeServiceId": null
        };

        function filtre() {
            ini();
        }

        function initFiltre() {
            vm.itemSearch ={
                "typeServiceId": null
            };
        }

        function ini() {
            var addItemData = {
                templateUrl: 'app/entities/template-message/template-message-dialog.html',
                controller: 'TemplateMessageDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'lg',
                resolve: {
                    entity: function () {
                        return {
                            libelle: null,
                            etiquette: null,
                            contenu: null,
                            langue: null,
                            destination:null,
                            id: null,
                            fseId:null,
                            typeServiceLibelle:null
                        };
                    }
                }
            };

            /**
             *Modification d'un élément
             */
            var editItemData = {
                templateUrl: 'app/entities/template-message/template-message-dialog.html',
                controller: 'TemplateMessageDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'lg',
                resolve: {
                    entity: null
                }
            };

            /**
             * Suppresion d'un élément
             */
            var deleteItemData = {
                templateUrl: 'app/entities/template-message/template-message-delete-dialog.html',
                controller: 'TemplateMessageDialogController',
                controllerAs: 'vm',
                size: 'sm',
                resolve: {
                    entity: null
                }
            };

            var detailItemData = {};

            var dialogData = {
                addData: addItemData,
                editData: editItemData,
                deleteData: deleteItemData,
                detailData: detailItemData
            };
            var entitySearchParams = function () {
                return {};
            };

            var entityParams = function () {
                return {
                    "typeServiceId": vm.itemSearch.typeServiceId
                };
            };

            var datas = {
                scope: $scope,
                vm: vm,
                entity: TemplateMessage,
                entitySearch: TemplateMessage,
                dialogData: dialogData,
                entitySearchParams: entitySearchParams,
                entityParams: entityParams
            };
            TableService.init(datas);
        }

        // vm.loadPage = loadPage;
        // vm.predicate = pagingParams.predicate;
        // vm.reverse = pagingParams.ascending;
        // vm.transition = transition;
        // vm.itemsPerPage = paginationConstants.itemsPerPage;

        //loadAll();

        // function loadAll () {
        //     TemplateMessage.query({
        //         page: pagingParams.page - 1,
        //         size: vm.itemsPerPage,
        //         sort: sort()
        //     }, onSuccess, onError);
        //     function sort() {
        //         var result = [vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc')];
        //         if (vm.predicate !== 'id') {
        //             result.push('id');
        //         }
        //         return result;
        //     }
        //     function onSuccess(data, headers) {
        //         vm.links = ParseLinks.parse(headers('link'));
        //         vm.totalItems = headers('X-Total-Count');
        //         vm.queryCount = vm.totalItems;
        //         vm.templateMessages = data;
        //         vm.page = pagingParams.page;
        //     }
        //     function onError(error) {
        //         AlertService.error(error.data.message);
        //     }
        // }
        //
        // function loadPage(page) {
        //     vm.page = page;
        //     vm.transition();
        // }
        //
        // function transition() {
        //     $state.transitionTo($state.$current, {
        //         page: vm.page,
        //         sort: vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc'),
        //         search: vm.currentSearch
        //     });
        // }
    }
})();
