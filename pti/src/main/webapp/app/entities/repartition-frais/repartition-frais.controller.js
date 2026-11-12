(function () {
    'use strict';

    angular
        .module('ptiApp')
        .controller('RepartitionFraisController', RepartitionFraisController);

    RepartitionFraisController.$inject = ['InitCrud', '$scope', 'RepartitionFrais', 'TypeService', 'RequestUtils'];

    function RepartitionFraisController(InitCrud, $scope, RepartitionFrais, TypeService, RequestUtils) {
        var vm = this;
        vm.title = "Repartitions des frais entre acteurs";
        vm.itemsFilter = [];
        vm.columns = [
            {
                name: 'typeServiceLibelle',
                title: 'Type service'
            }, {
                name: 'fseSrcLibelle',
                title: 'fseSource'
            }, {
                name: 'fseAcqLibelle',
                title: 'fseDest'
            }, {
                name: 'acteur',
                title: 'Acteur'
            }, {
                name: 'min',
                title: 'Min',
                filter: 'number'
            }, {
                name: 'max',
                title: 'Max',
                filter: 'number'
            }, {
                name: 'typeTarif',
                title: 'TypeTarif'
            }, {
                name: 'valeur',
                title: 'Valeur',

            }, {
                name: 'activated',
                title: 'Statut',
                type:'Boolean'
            }, {
                name: 'surFrais',
                title: 'Sur Frais',
                type:'Boolean'
            }];
        vm.actions = {
            view: false,
            edit: true,
            delete: true,
        };

        vm.acteurs = [{
            nom: "ACQ"
        }, {
            nom: "EM"
        }, {
            nom: "COMP"
        }];

        RequestUtils.GetOrganisationByTypeOptimise.get({ type: "FSE" }, function (datas) {
            vm.itemsFilter.push(
                [
                    {
                        name: 'fseSourceId',
                        model: null,
                        title: "Fse source",
                        datas: datas,
                        type: "select",
                        options: "data.id as data.libelleUniteOrganisation for data in col.datas"
                    }, {
                        name: 'fseDestId',
                        model: null,
                        title: "Fse Dest",
                        datas: datas,
                        type: "select",
                        options: "data.id as data.libelleUniteOrganisation for data in col.datas"
                    }
                ]
            );
            TypeService.queryAllOptimise({}, function (datas) {
                vm.itemsFilter.push(
                    [
                        {
                            name: 'typeServiceId',
                            model: null,
                            title: 'Choisir le type de service',
                            datas: datas,
                            type: "select",
                            options: "data.id as data.libelle for data in col.datas"
                        }, {
                            name: 'acteur',
                            model: null,
                            title: 'Choisir le type d\'acteur',
                            datas: datas,
                            type: "select",
                            options: "data.nom as data.nom for data in vm.acteurs"
                        }
                    ]
                );
            });
        });

        var datas = {
            vm: vm,
            entityDatas: {
                modeCalcul: null,
                appliqueSur: null,
                valeur: null,
                id: null,
                type: "TAXE"
            },
            entity: RepartitionFrais,
            addTemplateUrl: "repartition-frais/repartition-frais-dialog.html",
            deleteTemplateUrl: "repartition-frais/repartition-frais-delete-dialog.html",
            controller: "RepartitionFrais",
            entityParamsItem: {},
            entitySearchParamsItem: {},
            size: "md",
            hasFilter: true,
            scope: $scope,
            items: vm.itemsFilter
        };
        InitCrud.init(datas);
    }
})();
