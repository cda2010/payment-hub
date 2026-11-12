(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('AutorisationController', AutorisationController);

    AutorisationController.$inject = ['$scope','InitCrud','$state', 'Autorisation', 'ParseLinks', 'AlertService', 'paginationConstants', 'pagingParams'];

    function AutorisationController($scope,InitCrud,$state, Autorisation, ParseLinks, AlertService, paginationConstants, pagingParams) {

        var vm = this;
        vm.title="Paramétrages des autorisations";

        vm.columns = [{
            name: 'code',
            title: 'Code'
        },{
            name: 'uniteOrganisationLibelleUniteOrganisation',
            title: 'Organisation'
        },{
            name: 'typeServiceLibelle',
            title: 'Type de service'
        }];

        vm.actions={
            view:false,
            edit:true,
            delete:true,
        };

        var datas={
            vm:vm,
            entityDatas:{
                code: null,
                uniteOrganisationId:null,

                id: null
            },
            entity:Autorisation,
            addTemplateUrl:"autorisation/autorisation-dialog.html",
            deleteTemplateUrl:"autorisation/autorisation-delete-dialog.html",
            controller:"Autorisation",
            entityParamsItem:{},
            entitySearchParamsItem:{},
            size:"md",
            scope:$scope
        };
        InitCrud.init(datas);
    }
})();