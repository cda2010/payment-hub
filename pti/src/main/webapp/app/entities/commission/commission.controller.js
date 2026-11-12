(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('CommissionController', CommissionController);

    CommissionController.$inject = ['RequestUtils','InitCrud', '$scope', '$state', 'Commission', 'ParseLinks', 'AlertService', 'paginationConstants', 'pagingParams'];

    function CommissionController(RequestUtils,InitCrud, $scope, $state, Commission, ParseLinks, AlertService, paginationConstants, pagingParams) {

        var vm = this;
        vm.title = "Liste des commissions";
        vm.voirOperation=voirOperation;
        vm.itemsFilter=[];

        function voirOperation(commissionId){
            console.log(commissionId)
        }

        vm.values=[
            {
                id:true,
                nom:"Oui"
            },
            {
                id:false,
                nom:"Non"
            }
        ];
        
        RequestUtils.GetOrganisationByTypeOptimise.get({ type: "AGENCE" }, function(datas) {
            vm.pays = datas;
            vm.itemsFilter.push(
                [
                    {
                        name: 'uoId',
                        model:null,
                        title: "Choisir l'organisation",
                        datas:vm.pays,
                        type:"select",
                        options:"data.id as data.libelleUniteOrganisation for data in col.datas"
                    },{
                        name: 'dateEnregistrement',
                        model:null,
                        title: "date Enregistrement",
                        type:"inputDate",
                    },
                    {
                        name: 'dateFin',
                        model:null,
                        title: "date Fin",
                        type:"inputDate"
                    }
                ],[
                    {
                        name: 'estPaye',
                        model:null,
                        title: "Payé ?",
                        datas:vm.values,
                        type:"select",
                        options:"data.id as data.nom for data in col.datas"
                    }
                ]
            );
        });


        vm.columns = [{
            name: 'operationCodeOperation',
            title: 'Code Opération'
        },{
            name: 'dateEnregistrement',
            title: 'Date enregistrement',
            type: 'DateTime'
        },{
            name: 'dateFin',
            title: 'Date Fin',
            type: 'DateTime'
        },{
            name: 'montant',
            title: 'Montant',
            type:'Montant'
        },{
            name: 'montantOp',
            title: 'Montant transaction',
            type:'Montant'
        },
            {
            name: 'typeService',
            title: 'Type de service'

        }, {
            name: 'organisationLibelleUniteOrganisation',
            title: 'Organisation'
        }, {
            name: 'acteur',
            title: 'Acteur'
        }, {
            name: 'estPaye',
            title: 'est Payé ?',
            type: 'Boolean'
        }, {
            name: 'estComptabilise',
            title: 'est comptabilise ?',
            type: 'Boolean'
        },{
            name: 'estRegle',
            title: 'est Règlé ?',
            type: 'Boolean'
        }, {
            name: 'etat',
            title: 'Etat'
        }];
        vm.actions = {
            view: false,
            edit: false,
            delete: false,
            add:false,
            export:true
        };

        var datas = {
            bodyClass:"col-sm-12",
            vm: vm,
            entityDatas: {},
            //entity: Commission,
            entity: RequestUtils.CommissionOptimise,
            addTemplateUrl: "commission/commission-dialog.html",
            deleteTemplateUrl: "commission/commission-delete-dialog.html",
            controller: "Commission",
            entityParamsItem: {

            },
            entitySearchParamsItem: {},
            size: "md",
            scope: $scope,
            hasFilter:true,//filtre
            items:vm.itemsFilter
        };
        console.log('tok',datas);
        InitCrud.init(datas);
    }
})();
