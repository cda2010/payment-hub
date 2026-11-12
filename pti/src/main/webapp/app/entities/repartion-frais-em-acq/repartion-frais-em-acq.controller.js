(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('RepartionFraisEmAcqController', RepartionFraisEmAcqController);

    RepartionFraisEmAcqController.$inject = ['InitCrud', '$scope', '$state', 'RepartionFraisEmAcq', 'ParseLinks', 'AlertService', 'paginationConstants', 'pagingParams'];

    function RepartionFraisEmAcqController(InitCrud, $scope, $state, RepartionFraisEmAcq, ParseLinks, AlertService, paginationConstants, pagingParams) {

        var vm = this;
        vm.title = "Liste des repartion-frais-em-acqs";

        vm.columns = [
            {
                name: 'typeServiceLibelle',
                title: 'Type service'
            },{
            name: 'partEm',
            title: 'Part Em(%)'
        }, {
            name: 'partAcq',
            title: 'Part Acq(%)'
        }];

        vm.actions = {
            view: false,
            edit: true,
            delete: true,
        };

        var datas = {
            vm: vm,
            entityDatas: {},
            entity: RepartionFraisEmAcq,
            addTemplateUrl: "repartion-frais-em-acq/repartion-frais-em-acq-dialog.html",
            deleteTemplateUrl: "repartion-frais-em-acq/repartion-frais-em-acq-delete-dialog.html",
            controller: "RepartionFraisEmAcq",
            entityParamsItem: {},
            entitySearchParamsItem: {},
            size: "md",
            scope: $scope
        };
        InitCrud.init(datas);
    }
})();
